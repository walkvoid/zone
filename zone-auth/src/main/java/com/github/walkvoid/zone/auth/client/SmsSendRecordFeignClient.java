package com.github.walkvoid.zone.auth.client;

import com.github.walkvoid.wvframework.models.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 zone-system 短信发码/校验（定义在消费方）
 */
@FeignClient(name = ZoneSystemServiceName.SERVICE_NAME, contextId = "smsSendRecordFeignClient", path = "/internal/sms")
public interface SmsSendRecordFeignClient {

    @PostMapping("/send")
    ApiResult<Void> send(@RequestParam("target") String target,
                         @RequestParam("bizType") String bizType,
                         @RequestParam(value = "clientIp", required = false) String clientIp);

    @PostMapping("/verify")
    ApiResult<Boolean> verify(@RequestParam("target") String target,
                              @RequestParam("bizType") String bizType,
                              @RequestParam("code") String code);
}
