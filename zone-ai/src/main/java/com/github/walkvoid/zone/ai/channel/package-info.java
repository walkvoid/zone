/**
 * IM 通道模块：企业微信 / 飞书等机器人接入。
 *
 * <p>扩展方式：
 * <ol>
 *   <li>在 {@link ChannelType} 增加类型</li>
 *   <li>新增厂商包，继承 {@link AbstractChannelBotLifecycle}</li>
 *   <li>将协议消息映射为 {@link ChannelInboundMessage}</li>
 *   <li>复用 {@link ChannelMessageHandler}</li>
 * </ol>
 */
package com.github.walkvoid.zone.ai.channel;

import com.github.walkvoid.zone.ai.channel.core.AbstractChannelBotLifecycle;
import com.github.walkvoid.zone.ai.channel.core.ChannelInboundMessage;
import com.github.walkvoid.zone.ai.channel.core.ChannelMessageHandler;
import com.github.walkvoid.zone.ai.channel.core.ChannelType;