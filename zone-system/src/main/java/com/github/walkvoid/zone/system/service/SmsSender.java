package com.github.walkvoid.zone.system.service;

/**
 * 短信发送通道
 */
public interface SmsSender {

    /**
     * @return 通道回执 ID，无则返回 null
     */
    String send(String target, String code, String bizType);
}
