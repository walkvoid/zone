package com.github.walkvoid.zone.ai.chatclient;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public interface ChatModelInterceptor {


    default boolean callBefore(ChatResponse process, Prompt prompt){
        return true;
    }


    default boolean callAfter(ChatResponse process, Prompt prompt){
        return true;
    }


}
