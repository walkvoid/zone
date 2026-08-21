package com.github.walkvoid.zone.ai.service.impl;

import com.github.walkvoid.zone.ai.db.dao.McpServerConfigDAO;
import com.github.walkvoid.zone.ai.service.McpServerConfigService;
import com.github.walkvoid.zone.ai.db.entity.McpServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class McpServerConfigServiceImpl implements McpServerConfigService {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfigServiceImpl.class);

    @Autowired
    private McpServerConfigDAO dao;

    /** 运行中的 MCP 客户端注册表：serverCode -> clientHolder */
    private final ConcurrentHashMap<String, McpClientHolder> clientRegistry = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> start(String serverCode) {
        Map<String, Object> result = new HashMap<>();
        McpServerConfig config = dao.selectByCode(serverCode);
        if (config == null) {
            result.put("success", false);
            result.put("message", "MCP服务不存在: " + serverCode);
            return result;
        }
        if (config.getStatus() != null && config.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "MCP服务已禁用，无法启动: " + serverCode);
            return result;
        }
        if (clientRegistry.containsKey(serverCode)) {
            result.put("success", false);
            result.put("message", "MCP服务已在运行中: " + serverCode);
            return result;
        }

        try {
            McpClientHolder holder = doStart(config);
            clientRegistry.put(serverCode, holder);
            updateRunningStatus(config, 1);
            log.info("MCP服务启动成功: {}", serverCode);
            result.put("success", true);
            result.put("message", "启动成功: " + serverCode);
        } catch (Exception e) {
            log.error("MCP服务启动失败: {}", serverCode, e);
            updateRunningStatus(config, 2);
            result.put("success", false);
            result.put("message", "启动失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> stop(String serverCode) {
        Map<String, Object> result = new HashMap<>();
        McpClientHolder holder = clientRegistry.remove(serverCode);
        if (holder == null) {
            result.put("success", false);
            result.put("message", "MCP服务未在运行: " + serverCode);
            return result;
        }

        try {
            doStop(holder);
            McpServerConfig config = dao.selectByCode(serverCode);
            if (config != null) {
                updateRunningStatus(config, 0);
            }
            log.info("MCP服务已停止: {}", serverCode);
            result.put("success", true);
            result.put("message", "已停止: " + serverCode);
        } catch (Exception e) {
            log.error("MCP服务停止异常: {}", serverCode, e);
            result.put("success", false);
            result.put("message", "停止异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> startAll() {
        Map<String, Object> result = new HashMap<>();
        List<McpServerConfig> enabledList = dao.selectEnabled();
        int successCount = 0;
        int failCount = 0;
        List<String> failedCodes = new ArrayList<>();

        for (McpServerConfig config : enabledList) {
            // 跳过已在运行的
            if (clientRegistry.containsKey(config.getServerCode())) {
                continue;
            }
            try {
                McpClientHolder holder = doStart(config);
                clientRegistry.put(config.getServerCode(), holder);
                updateRunningStatus(config, 1);
                successCount++;
            } catch (Exception e) {
                log.error("批量启动失败: {}", config.getServerCode(), e);
                updateRunningStatus(config, 2);
                failCount++;
                failedCodes.add(config.getServerCode());
            }
        }

        result.put("success", failCount == 0);
        result.put("startedCount", successCount);
        result.put("failedCount", failCount);
        result.put("failedCodes", failedCodes);
        return result;
    }

    @Override
    public Map<String, Object> stopAll() {
        Map<String, Object> result = new HashMap<>();
        int count = 0;

        for (Map.Entry<String, McpClientHolder> entry : clientRegistry.entrySet()) {
            try {
                doStop(entry.getValue());
                McpServerConfig config = dao.selectByCode(entry.getKey());
                if (config != null) {
                    updateRunningStatus(config, 0);
                }
                count++;
            } catch (Exception e) {
                log.error("批量停止异常: {}", entry.getKey(), e);
            }
        }
        clientRegistry.clear();

        result.put("success", true);
        result.put("stoppedCount", count);
        return result;
    }

    @Override
    public Map<String, Object> restart(String serverCode) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> stopResult = stop(serverCode);
        if (!Boolean.TRUE.equals(stopResult.get("success"))) {
            // 如果本来就没在运行，直接尝试启动
            if (clientRegistry.containsKey(serverCode)) {
                return stopResult;
            }
        }
        return start(serverCode);
    }

    @Override
    public Map<String, Object> getRunningStatus(String serverCode) {
        Map<String, Object> result = new HashMap<>();
        McpClientHolder holder = clientRegistry.get(serverCode);
        result.put("running", holder != null);
        result.put("serverCode", serverCode);
        if (holder != null) {
            result.put("startTime", holder.startTime);
        }
        McpServerConfig config = dao.selectByCode(serverCode);
        if (config != null) {
            result.put("runningStatus", config.getRunningStatus());
            result.put("enabled", config.getStatus() == 1);
        }
        return result;
    }

    @Override
    public List<String> listRunningCodes() {
        return new ArrayList<>(clientRegistry.keySet());
    }

    // ==================== CRUD ====================

    @Override
    public int insert(McpServerConfig entity) {
        return dao.insert(entity);
    }

    @Override
    public int update(McpServerConfig entity) {
        return dao.updateById(entity);
    }

    @Override
    public int delete(Long id) {
        McpServerConfig config = dao.selectById(id);
        if (config != null && clientRegistry.containsKey(config.getServerCode())) {
            stop(config.getServerCode());
        }
        return dao.deleteById(id);
    }

    @Override
    public McpServerConfig getById(Long id) {
        return dao.selectById(id);
    }

    @Override
    public McpServerConfig getByCode(String serverCode) {
        return dao.selectByCode(serverCode);
    }

    @Override
    public List<McpServerConfig> listAll() {
        return dao.selectList(new McpServerConfig());
    }

    // ==================== 内部方法 ====================

    /**
     * 执行实际的 MCP 客户端启动逻辑。
     * 当前为占位实现，后续接入具体 MCP 客户端 SDK 时替换此方法。
     */
    private McpClientHolder doStart(McpServerConfig config) {
        McpClientHolder holder = new McpClientHolder();
        holder.serverCode = config.getServerCode();
        holder.startTime = LocalDateTime.now();

        // TODO: 根据 transportType 初始化实际 MCP 客户端连接
        // if ("stdio".equals(config.getTransportType())) {
        //     // McpClient.using(config.getCommand(), parseArgs(config.getArgs()))
        //     //     .env(parseEnv(config.getEnvVars()))
        //     //     .connect();
        // } else if ("sse".equals(config.getTransportType())) {
        //     // McpClient.using(config.getUrl())
        //     //     .headers(parseHeaders(config.getHeaders()))
        //     //     .connect();
        // }

        log.info("MCP客户端占位初始化完成: transport={}, code={}", config.getTransportType(), config.getServerCode());
        return holder;
    }

    /**
     * 执行实际的 MCP 客户端关闭逻辑。
     */
    private void doStop(McpClientHolder holder) {
        // TODO: 调用实际 MCP 客户端的 close/disconnect 方法
        log.info("MCP客户端占位关闭完成: {}", holder.serverCode);
    }

    private void updateRunningStatus(McpServerConfig config, int runningStatus) {
        McpServerConfig update = new McpServerConfig();
        update.setId(config.getId());
        update.setRunningStatus(runningStatus);
        dao.updateById(update);
    }

    /**
     * MCP 客户端运行时的持有对象，后续替换为实际的客户端引用。
     */
    private static class McpClientHolder {
        String serverCode;
        LocalDateTime startTime;
        // TODO: 替换为实际 MCP 客户端类型，如 McpClient / McpSyncClient
        // Object client;
    }
}
