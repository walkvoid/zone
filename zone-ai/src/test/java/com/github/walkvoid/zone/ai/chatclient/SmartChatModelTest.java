package com.github.walkvoid.zone.ai.chatclient;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmartChatModelTest {

    @Test
    void callRunsProcessorsAroundDelegate() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.call(any(Prompt.class))).thenReturn(response("ok"));
        List<String> trail = new ArrayList<>();
        SmartChatModel model = new SmartChatModel(delegate, List.of((prompt, chain) -> {
            trail.add("before");
            ChatResponse response = chain.next(prompt);
            trail.add("after:" + text(response));
            return response;
        }));

        assertEquals("ok", model.call("hi"));
        assertEquals(List.of("before", "after:ok"), trail);
        verify(delegate).call(any(Prompt.class));
    }

    @Test
    void processorsRunOuterToInnerByOrder() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.call(any(Prompt.class))).thenReturn(response("ok"));
        List<String> trail = new ArrayList<>();
        ChatModelProcessor inner = ordered(20, "inner", trail);
        ChatModelProcessor outer = ordered(10, "outer", trail);

        new SmartChatModel(delegate, List.of(inner, outer)).call(new Prompt("hi"));

        assertEquals(List.of("outer-before", "inner-before", "inner-after", "outer-after"), trail);
    }

    @Test
    void processorCanRewritePromptAndShortCircuit() {
        ChatModel delegate = mock(ChatModel.class);
        ChatModelProcessor cache = (prompt, chain) -> {
            if ("cached".equals(prompt.getContents())) {
                return response("from-cache");
            }
            return chain.next(new Prompt(prompt.getContents() + "!"));
        };
        when(delegate.call(any(Prompt.class))).thenAnswer(invocation -> {
            Prompt prompt = invocation.getArgument(0);
            return response("model:" + prompt.getContents());
        });
        SmartChatModel model = new SmartChatModel(delegate, List.of(cache));

        assertEquals("from-cache", text(model.call(new Prompt("cached"))));
        verify(delegate, never()).call(any(Prompt.class));
        assertEquals("model:ask!", text(model.call(new Prompt("ask"))));
    }

    @Test
    void processorSeesDelegateFailure() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.call(any(Prompt.class))).thenThrow(new IllegalStateException("boom"));
        AtomicInteger failures = new AtomicInteger();
        SmartChatModel model = new SmartChatModel(delegate, List.of((prompt, chain) -> {
            try {
                return chain.next(prompt);
            } catch (RuntimeException ex) {
                failures.incrementAndGet();
                throw ex;
            }
        }));

        assertThrows(IllegalStateException.class, () -> model.call(new Prompt("hi")));
        assertEquals(1, failures.get());
    }

    @Test
    void streamGoesThroughProcessStream() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.stream(any(Prompt.class))).thenReturn(Flux.just(response("a"), response("b")));
        List<String> trail = new ArrayList<>();
        ChatModelProcessor processor = new ChatModelProcessor() {
            @Override
            public ChatResponse process(Prompt prompt, Chain chain) {
                return chain.next(prompt);
            }

            @Override
            public Flux<ChatResponse> processStream(Prompt prompt, Chain chain) {
                trail.add("stream-before");
                return chain.nextStream(prompt).doOnComplete(() -> trail.add("stream-after"));
            }
        };

        List<String> chunks = new SmartChatModel(delegate, List.of(processor))
                .stream(new Prompt("hi"))
                .map(SmartChatModelTest::text)
                .collectList()
                .block();

        assertEquals(List.of("a", "b"), chunks);
        assertEquals(List.of("stream-before", "stream-after"), trail);
    }

    @Test
    void wrapSkipsAlreadySmartChatModel() {
        ChatModel delegate = mock(ChatModel.class);
        SmartChatModel smart = new SmartChatModel(delegate, List.of());
        assertSame(smart, SmartChatModel.wrap(smart, List.of((prompt, chain) -> chain.next(prompt))));
    }

    @Test
    void getOptionsDelegates() {
        ChatModel delegate = mock(ChatModel.class);
        ChatOptions options = ChatOptions.builder().model("m1").build();
        when(delegate.getOptions()).thenReturn(options);
        assertSame(options, new SmartChatModel(delegate, List.of()).getOptions());
    }

    @Test
    void emptyProcessorsPassThrough() {
        ChatModel delegate = mock(ChatModel.class);
        when(delegate.call(any(Prompt.class))).thenReturn(response("ok"));
        assertEquals("ok", text(new SmartChatModel(delegate, null).call(new Prompt("hi"))));
        assertTrue(new SmartChatModel(delegate, null).processors().isEmpty());
    }

    private static ChatModelProcessor ordered(int order, String name, List<String> trail) {
        return new ChatModelProcessor() {
            @Override
            public ChatResponse process(Prompt prompt, Chain chain) {
                trail.add(name + "-before");
                ChatResponse response = chain.next(prompt);
                trail.add(name + "-after");
                return response;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }

    private static ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    private static String text(ChatResponse response) {
        return response.getResult().getOutput().getText();
    }
}
