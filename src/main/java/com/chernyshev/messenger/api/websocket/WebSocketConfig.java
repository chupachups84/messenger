package com.chernyshev.messenger.api.websocket;

import com.chernyshev.messenger.api.services.MessageService;
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
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final WebSocketUtils webSocketUtils;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "websocket/messages/{id}").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyWebSocketHandler(messageService,userRepository,webSocketUtils);
    }
}
