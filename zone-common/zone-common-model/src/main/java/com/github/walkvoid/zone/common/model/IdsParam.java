package com.github.walkvoid.zone.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量ID请求参数
 *
 * @author walkvoid
 */
@Data
@Schema(description = "批量ID请求")
public class IdsParam {

    @Schema(description = "ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}
