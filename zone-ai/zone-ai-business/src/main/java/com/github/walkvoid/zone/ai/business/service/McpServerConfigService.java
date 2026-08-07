package com.github.walkvoid.zone.ai.business.service;

import com.github.walkvoid.zone.ai.model.entity.McpServerConfig;
import java.util.List;
import java.util.Map;

public interface McpServerConfigService {

    /** 启动指定MCP服务 */
    Map<String, Object> start(String serverCode);

    /** 停止指定MCP服务 */
    Map<String, Object> stop(String serverCode);

    /** 启动所有已启用的MCP服务 */
    Map<String, Object> startAll();

    /** 停止所有运行中的MCP服务 */
    Map<String, Object> stopAll();

    /** 重启指定MCP服务 */
    Map<String, Object> restart(String serverCode);

    /** 获取运行状态 */
    Map<String, Object> getRunningStatus(String serverCode);

    /** 获取所有运行中的服务编码 */
    List<String> listRunningCodes();

    // CRUD
    int insert(McpServerConfig entity);
    int update(McpServerConfig entity);
    int delete(Long id);
    McpServerConfig getById(Long id);
    McpServerConfig getByCode(String serverCode);
    List<McpServerConfig> listAll();
}
