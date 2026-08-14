/**
 * IM 通道模块：企业微信 / 飞书等机器人接入。
 *
 * <p>扩展方式：
 * <ol>
 *   <li>在 {@link com.github.walkvoid.zone.ai.business.channel.core.ChannelType} 增加类型</li>
 *   <li>新增厂商包，继承 {@link com.github.walkvoid.zone.ai.business.channel.core.AbstractChannelBotLifecycle}</li>
 *   <li>将协议消息映射为 {@link com.github.walkvoid.zone.ai.business.channel.core.ChannelInboundMessage}</li>
 *   <li>复用 {@link com.github.walkvoid.zone.ai.business.channel.core.ChannelMessageHandler}</li>
 * </ol>
 */
package com.github.walkvoid.zone.ai.business.channel;
