package com.github.walkvoid.zone.system.service;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.system.model.enums.SmsBizTypeEnum;

/**
 * 短信发送记录：发码与校验
 */
public interface SmsSendRecordService {

    /**
     * 生成并发送验证码（落库）
     */
    ApiResult<Void> sendCode(String target, SmsBizTypeEnum bizType, String clientIp);

    /**
     * 校验验证码，成功则标记已使用
     */
    ApiResult<Boolean> verifyCode(String target, SmsBizTypeEnum bizType, String code);
}
