package com.github.walkvoid.zone.ai.chatclient;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

/**
 * {@link org.springframework.ai.chat.model.ChatModel} 调用切面。
 * <p>
 * 必须调用 {@link Chain#next(Prompt)} / {@link Chain#nextStream(Prompt)} 才会真正请求大模型；
 * 可在前后改 Prompt / ChatResponse，也可短路（缓存、降级）。{@link #getOrder()} 越小越靠外。
 * <p>
 * 同步与流式是两条链：只覆盖 {@link #process} 时，{@code stream()} 仍会直达下一环。
 */
@FunctionalInterface
public interface ChatModelProcessor extends Ordered {

    ChatResponse process(Prompt prompt, Chain chain);

    default Flux<ChatResponse> processStream(Prompt prompt, Chain chain) {
        return chain.nextStream(prompt);
    }

    @Override
    default int getOrder() {
        return 0;
    }

    interface Chain {

        ChatResponse next(Prompt prompt);

        Flux<ChatResponse> nextStream(Prompt prompt);
    }
}
