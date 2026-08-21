package com.github.walkvoid.zone.ai.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MCP Server配置实体
 *
 * @author walkvoid
 */
@Data
@TableName("mcp_server_config")
public class McpServerConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /** 服务编码 */
    private String serverCode;

    /** 服务名称 */
    private String serverName;

    /** 传输类型：stdio / sse / streamable-http */
    private String transportType;

    /** 启动命令（stdio模式） */
    private String command;

    /** 命令参数（JSON数组） */
    private String args;

    /** 服务URL（sse / streamable-http模式） */
    private String url;

    /** 环境变量（JSON） */
    private String envVars;

    /** 自定义请求头（JSON） */
    private String headers;

    /** 超时时间（毫秒） */
    private Long timeoutMs;

    /** 启用状态：0=禁用，1=启用 */
    private Integer status;

    /** 运行状态：0=已停止，1=运行中，2=异常 */
    private Integer runningStatus;

    /** 描述 */
    private String description;

    /** 创建人ID */
    private Long createId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人ID */
    private Long updateId;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
