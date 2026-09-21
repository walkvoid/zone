package com.github.walkvoid.zone.ai.chatclient;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

public class LogChatModelProcessor implements ChatModelProcessor {


    @Override
    public ChatResponse process(Prompt prompt, Chain chain) {

        ChatResponse next = chain.next(prompt);



        return next;
    }

    @Override
    public Flux<ChatResponse> processStream(Prompt prompt, Chain chain) {

        Flux<ChatResponse> chatResponseFlux = chain.nextStream(prompt);

        return chatResponseFlux;
    }


}
