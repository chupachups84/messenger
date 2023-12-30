package com.chernyshev.messenger.websocket;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.dtos.MessageResponse;
import com.chernyshev.messenger.services.MessageService;
import com.chernyshev.messenger.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class MyWebSocketHandler implements WebSocketHandler {
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final WebSocketUtils webSocketUtils;
    private final Map<Long, Map<Long, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //проверка пользователя
        var user = userRepository.findByUsername(session.getPrincipal().getName()).orElseThrow();
        if(!user.isActive()){
            session.sendMessage(new TextMessage("Не удалось подключиться"));
            session.close();
        }
        //извлекаем id из подключения
        Long receiverId = webSocketUtils.getId(session.getUri().getPath());
        if(!userRepository.existsById(receiverId)) {
            session.sendMessage(new TextMessage("Пользователь не найден"));
            session.close();
        }
        Long senderId= userRepository.findByUsername(session.getPrincipal().getName()).orElseThrow().getId();
        //соединяем сессии
        if (!userSessions.containsKey(senderId)) userSessions.put(senderId, new HashMap<>());
        userSessions.get(senderId).put(receiverId,session);
        //отправляем историю сообщений
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageService.getMessageHistory(session.getPrincipal().getName(), receiverId))));
        }catch (IllegalStateException e){
            session.sendMessage(new TextMessage(e.getMessage()));
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            //извлекаем данные из сессии
            Long receiverId = webSocketUtils.getId(session.getUri().getPath());
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            //сохраняем сообщение в бд
            MessageResponse messageResponse = messageService.sendMessage(session.getPrincipal().getName(),receiverId,message.getPayload().toString());
            //отправляем сообщение в сессию пользователя
            Long senderId = userRepository.findByUsername(session.getPrincipal().getName()).orElseThrow().getId();
            if(userSessions.containsKey(receiverId))
                userSessions.get(receiverId).get(senderId)
                        .sendMessage(new TextMessage(objectMapper.writeValueAsString(messageResponse)));
        } catch (FriendshipException e){
            session.sendMessage(new TextMessage(e.getMessage()));
            session.close();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        userSessions.remove(userRepository.findByUsername(session.getPrincipal().getName()).orElseThrow().getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

