package com.github.walkvoid.zone.ai.business.channel.core;

import com.github.walkvoid.zone.ai.business.channel.feishu.FeishuBotProperties;
import com.github.walkvoid.zone.ai.business.channel.weixin.WeiXinAiBotProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通道总配置。前缀 {@code zone.channel}。
 */
@ConfigurationProperties(prefix = "zone.ai.channel")
public class ChannelProperties {

    /**
     * 总开关：false 时所有通道都不启动。
     */
    private boolean enabled = false;

    private WeiXinAiBotProperties weixin = new WeiXinAiBotProperties();

    private FeishuBotProperties feishu = new FeishuBotProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public WeiXinAiBotProperties getWeixin() {
        return weixin;
    }

    public void setWeixin(WeiXinAiBotProperties weixin) {
        this.weixin = weixin;
    }

    public FeishuBotProperties getFeishu() {
        return feishu;
    }

    public void setFeishu(FeishuBotProperties feishu) {
        this.feishu = feishu;
    }
}
