package com.chernyshev.messenger.api.config;

import com.chernyshev.messenger.api.services.CustomWebSocketHandler;
import com.chernyshev.messenger.store.repositories.MessageRepository;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "messages/{username}").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new CustomWebSocketHandler(userRepository,messageRepository);
    }
}
