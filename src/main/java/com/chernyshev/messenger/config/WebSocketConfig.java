package com.chernyshev.messenger.config;

import com.chernyshev.messenger.repositories.UserRepository;
import com.chernyshev.messenger.services.MessageService;
import com.chernyshev.messenger.services.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final MessageService messageService;
    private final UserRepository userRepository;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MessageWebSocketHandler(messageService,userRepository), "messages/{username}").setAllowedOrigins("*");
    }
}
