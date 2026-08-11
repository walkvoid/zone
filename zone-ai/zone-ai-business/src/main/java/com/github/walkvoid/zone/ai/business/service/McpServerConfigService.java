package com.github.walkvoid.zone.ai.business.service;

import com.github.walkvoid.zone.ai.model.entity.McpServerConfig;
import java.util.List;
import java.util.Map;

/**
 * MCP Server 配置 Service
 *
 * @author walkvoid
 */
public interface McpServerConfigService {

    /**
     * 启动指定MCP服务
     *
     * @param serverCode 服务编码
     * @return 执行结果，包含success、message等字段
     */
    Map<String, Object> start(String serverCode);

    /**
     * 停止指定MCP服务
     *
     * @param serverCode 服务编码
     * @return 执行结果
     */
    Map<String, Object> stop(String serverCode);

    /**
     * 启动所有已启用的MCP服务
     *
     * @return 执行结果，包含startedCount、failedCount等
     */
    Map<String, Object> startAll();

    /**
     * 停止所有运行中的MCP服务
     *
     * @return 执行结果，包含stoppedCount
     */
    Map<String, Object> stopAll();

    /**
     * 重启指定MCP服务
     *
     * @param serverCode 服务编码
     * @return 执行结果
     */
    Map<String, Object> restart(String serverCode);

    /**
     * 获取运行状态
     *
     * @param serverCode 服务编码
     * @return running、serverCode、runningStatus、enabled等
     */
    Map<String, Object> getRunningStatus(String serverCode);

    /**
     * 获取所有运行中的服务编码
     *
     * @return 服务编码列表
     */
    List<String> listRunningCodes();

    // ==================== CRUD ====================

    /**
     * 新增配置
     */
    int insert(McpServerConfig entity);

    /**
     * 更新配置
     */
    int update(McpServerConfig entity);

    /**
     * 删除配置，同时停止对应服务
     */
    int delete(Long id);

    /**
     * 根据ID查询
     */
    McpServerConfig getById(Long id);

    /**
     * 根据编码查询
     */
    McpServerConfig getByCode(String serverCode);

    /**
     * 查询全部配置
     */
    List<McpServerConfig> listAll();
}
