package com.github.walkvoid.zone.ai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志搜索请求参数 DTO
 * <p>
 * 对应 beecloud.llschain.com 日志检索平台的 API 请求参数。
 *
 * @author jiangjunqing
 * @date 2026/8/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogSearchRequestDTO {

    /** 搜索字符串（支持 traceId / 关键词） */
    private String searchStr;

    /** 搜索类型，默认 devops */
    @Builder.Default
    private String searchType = "devops";

    /** 环境标识，如 dev|jinkoscf */
    private String env;

    /** 环境实例列表 */
    private String envInstance;

    /** 开始时间（ISO 8601，如 2026-08-11T09:02:12Z） */
    private String gte;

    /** 结束时间（ISO 8601） */
    private String lte;

    /** 额外展示的列，JSON 数组，如 ["message"] */
    @Builder.Default
    private String otherColumnsName = "[\"message\"]";

    /** 时间步长，默认 1800s */
    @Builder.Default
    private String currentTimeStep = "1800s";
}
