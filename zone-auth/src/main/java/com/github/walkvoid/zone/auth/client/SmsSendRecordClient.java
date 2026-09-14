package com.github.walkvoid.zone.auth.client;

import com.github.walkvoid.wvframework.models.ApiResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 调用 zone-system 短信发码/校验（定义在消费方）
 */
@HttpExchange("/internal/sms")
public interface SmsSendRecordClient {

    @PostExchange("/send")
    ApiResult<Void> send(@RequestParam("target") String target,
                         @RequestParam("bizType") String bizType,
                         @RequestParam(value = "clientIp", required = false) String clientIp);

    @PostExchange("/verify")
    ApiResult<Boolean> verify(@RequestParam("target") String target,
                              @RequestParam("bizType") String bizType,
                              @RequestParam("code") String code);
}
