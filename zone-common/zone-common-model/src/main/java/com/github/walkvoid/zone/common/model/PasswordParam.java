package com.github.walkvoid.zone.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 密码请求参数
 *
 * @author walkvoid
 */
@Data
@Schema(description = "密码请求")
public class PasswordParam {

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
