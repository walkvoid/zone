package com.github.walkvoid.zone.ai.controller;

import com.github.walkvoid.zone.ai.model.dto.ChatStreamRequest;
import com.github.walkvoid.zone.ai.service.ChatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Web 端 AI 聊天（SSE 流式）
 */
@Tag(name = "AI聊天")
@RestController
@RequestMapping("/ai/chat")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-chat-stream");
        t.setDaemon(true);
        return t;
    });

    @Autowired
    private ChatStreamService chatStreamService;

    @Operation(summary = "流式聊天（SSE）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> log.warn("AI chat SSE timeout"));
        emitter.onError(ex -> log.warn("AI chat SSE error: {}", ex.getMessage()));

        executor.execute(() -> {
            try {
                chatStreamService.stream(request, delta -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(Map.of("delta", delta), MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of("done", true), MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI chat stream failed: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("error", e.getMessage() == null ? "stream failed" : e.getMessage()),
                                    MediaType.APPLICATION_JSON));
                    emitter.complete();
                } catch (Exception sendEx) {
                    emitter.completeWithError(e);
                }
            }
        });

        return emitter;
    }
}
