package com.github.walkvoid.zone.ai.chatclient;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link ChatModel} 代理：把每次 {@code call}/{@code stream} 走一遍 {@link ChatModelProcessor} 链。
 * <p>
 * 不覆盖 {@code call(String)} 等便利方法，让它们走接口默认实现，从而进入本类的 {@link #call(Prompt)}。
 */
public final class SmartChatModel implements ChatModel {

    private final List<ChatModelProcessor> processors;

    private final ChatModel delegate;

    public SmartChatModel(ChatModel delegate, List<ChatModelProcessor> processors) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.processors = copyAndSort(processors);
    }

    public static ChatModel wrap(ChatModel delegate, List<ChatModelProcessor> processors) {
        if (delegate instanceof SmartChatModel) {
            return delegate;
        }
        return new SmartChatModel(delegate, processors);
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Objects.requireNonNull(prompt, "prompt must not be null");
        return new ProcessorChain(0).next(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Objects.requireNonNull(prompt, "prompt must not be null");
        return new ProcessorChain(0).nextStream(prompt);
    }

    @Override
    public ChatOptions getOptions() {
        return delegate.getOptions();
    }

    List<ChatModelProcessor> processors() {
        return processors;
    }

    ChatModel delegate() {
        return delegate;
    }

    private static List<ChatModelProcessor> copyAndSort(List<ChatModelProcessor> processors) {
        if (processors == null || processors.isEmpty()) {
            return List.of();
        }
        List<ChatModelProcessor> copy = new ArrayList<>(processors.size());
        for (ChatModelProcessor processor : processors) {
            if (processor != null) {
                copy.add(processor);
            }
        }
        AnnotationAwareOrderComparator.sort(copy);
        return List.copyOf(copy);
    }

    private final class ProcessorChain implements ChatModelProcessor.Chain {

        private final int index;

        private ProcessorChain(int index) {
            this.index = index;
        }

        @Override
        public ChatResponse next(Prompt prompt) {
            Objects.requireNonNull(prompt, "prompt must not be null");
            if (index < processors.size()) {
                return processors.get(index).process(prompt, new ProcessorChain(index + 1));
            }
            return delegate.call(prompt);
        }

        @Override
        public Flux<ChatResponse> nextStream(Prompt prompt) {
            Objects.requireNonNull(prompt, "prompt must not be null");
            if (index < processors.size()) {
                return processors.get(index).processStream(prompt, new ProcessorChain(index + 1));
            }
            return delegate.stream(prompt);
        }
    }
}
