package com.chernyshev.messenger.api.config;

import com.chernyshev.messenger.api.services.MessageWebSocketHandler;
import com.chernyshev.messenger.store.repositories.FriendRepository;
import com.chernyshev.messenger.store.repositories.MessageRepository;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MessageWebSocketHandler(userRepository,messageRepository,friendRepository), "messages/{username}").setAllowedOrigins("*");
    }
}
