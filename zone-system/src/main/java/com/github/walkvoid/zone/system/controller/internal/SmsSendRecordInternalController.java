package com.github.walkvoid.zone.system.controller.internal;

import com.github.walkvoid.wvframework.models.ApiResult;
import com.github.walkvoid.zone.system.model.enums.SmsBizTypeEnum;
import com.github.walkvoid.zone.system.service.SmsSendRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信发码/校验内部接口（供 Feign 调用）
 */
@RestController
@RequestMapping("/internal/sms")
public class SmsSendRecordInternalController {

    @Autowired
    private SmsSendRecordService smsSendRecordService;

    @PostMapping("/send")
    public ApiResult<Void> send(@RequestParam String target,
                                @RequestParam SmsBizTypeEnum bizType,
                                @RequestParam(required = false) String clientIp) {
        return smsSendRecordService.sendCode(target, bizType, clientIp);
    }

    @PostMapping("/verify")
    public ApiResult<Boolean> verify(@RequestParam String target,
                                     @RequestParam SmsBizTypeEnum bizType,
                                     @RequestParam String code) {
        return smsSendRecordService.verifyCode(target, bizType, code);
    }
}
