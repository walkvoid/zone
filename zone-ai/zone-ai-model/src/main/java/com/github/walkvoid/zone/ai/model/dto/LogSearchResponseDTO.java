package com.github.walkvoid.zone.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 日志搜索结果 DTO
 *
 * @author jiangjunqing
 * @date 2026/8/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogSearchResponseDTO {

    /** 请求是否成功 */
    private boolean success;

    /** 日志数据行（每行是一个 Map） */
    private List<Map<String, Object>> rows;

    /** 匹配总条数 */
    private long total;

    /** 原始响应（调试用） */
    private String rawResponse;
}
