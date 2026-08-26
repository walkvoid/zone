package com.github.walkvoid.zone.system.service.impl;

import com.github.walkvoid.zone.system.service.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock 短信发送：仅打日志，便于本地联调
 */
@Component
public class MockSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsSender.class);

    @Override
    public String send(String target, String code, String bizType) {
        log.info("[MockSms] target={}, bizType={}, code={}", target, bizType, code);
        return "mock-" + System.currentTimeMillis();
    }
}
