package com.github.walkvoid.zone.ai.chatclient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
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

    private final ChatModel delegate;

    public SmartChatModel(ChatModel delegate, List<ChatModelProcessor> processors) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
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

    private final class InterceptorChain {

        private static final Log log = LogFactory.getLog(InterceptorChain.class);

        private final List<ChatModelInterceptor> interceptorList = new ArrayList<>();

        private int interceptorIndex = -1;

        boolean applyPreHandle(ChatResponse call, Prompt prompt){
            for (int i = 0; i < this.interceptorList.size(); i++) {
                ChatModelInterceptor interceptor = this.interceptorList.get(i);
                if (!interceptor.callBefore(call, prompt)) {
                    return false;
                }
                this.interceptorIndex = i;
            }
            return true;
        }

        /**
         * Apply postHandle methods of registered interceptors.
         */
        void applyPostHandle(ChatResponse call, Prompt prompt) {
            for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
                ChatModelInterceptor interceptor = this.interceptorList.get(i);
                interceptor.callAfter(call, prompt);
            }
        }
    }
}
