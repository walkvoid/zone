package com.github.walkvoid.zone.ai.chatclient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

public final class SmartChatClient implements ChatClient {

    private final ChatClientConfigurer configurer;

    private final ChatClient delegate;   // 真正的 DefaultChatClient
    private final ChatClientFactory factory;


    SmartChatClient(ChatClientFactory factory,
                    ChatClientConfigurer configurer,
                    ChatClient delegate) {
        this.factory = factory;
        this.configurer = configurer;
        this.delegate = delegate;
    }

    public SmartChatClient switchModel(String model){
        return factory.get(configurer.useModel(model));
    }

    @Override
    public ChatClientRequestSpec prompt() {
        return delegate.prompt();
    }

    @Override
    public ChatClientRequestSpec prompt(String content) {
        return delegate.prompt(content);
    }

    @Override
    public ChatClientRequestSpec prompt(Prompt prompt) {
        return delegate.prompt(prompt);
    }

    @Override
    public Builder mutate() {
        return delegate.mutate();
    }


}
