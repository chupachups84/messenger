package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.dtos.ErrorDto;
import com.chernyshev.messenger.api.dtos.MessageDto;
import com.chernyshev.messenger.api.exceptions.ForbiddenException;
import com.chernyshev.messenger.api.exceptions.NotFoundException;
import com.chernyshev.messenger.store.models.MessageEntity;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.MessageRepository;
import com.chernyshev.messenger.store.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CustomWebSocketHandler implements WebSocketHandler {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final Map<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String USER_NOT_FOUND ="Пользователь %s не найден";
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        try {
            String senderUsername = session.getPrincipal().getName();
            String receiverUsername = getUsername(session.getUri().getPath());
            var sender = userRepository.findByUsername(senderUsername)
                    .filter(UserEntity::isActive).orElseThrow(
                            () -> new NotFoundException(
                                    String.format(USER_NOT_FOUND,senderUsername)
                            )
                    );
            var receiver = userRepository.findByUsername(receiverUsername)
                    .filter(UserEntity::isActive).orElseThrow(
                            () -> new NotFoundException(
                                    String.format(USER_NOT_FOUND,receiverUsername)
                            )
                    );

            if(receiver.isReceiveMessagesFriendOnly()&&!userRepository.areFriends(senderUsername,receiverUsername))
                throw new ForbiddenException("Пользователь ограничил получение сообщений только своим кругом друзей");

            if (!userSessions.containsKey(sender.getUsername()))
                userSessions.put(sender.getUsername(), new HashMap<>());
            userSessions.get(sender.getUsername()).put(receiverUsername,session);

            messageRepository.findBySenderAndReceiver(sender, receiver).forEach(
                    message -> {
                        try {
                            session.sendMessage(
                                    new TextMessage(
                                            objectMapper.writeValueAsString(createMessageDto(message))
                                    )
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (NotFoundException e){
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .error("Not Found")
                                    .errorDescription(e.getMessage())
                                    .build())
            ));
            session.close();
        } catch (ForbiddenException e){
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .error("Forbidden")
                                    .errorDescription(e.getMessage())
                                    .build())
            ));
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {

            String senderUsername = session.getPrincipal().getName();
            String receiverUsername = getUsername(session.getUri().getPath());
            var sender = userRepository.findByUsername(senderUsername)
                    .filter(UserEntity::isActive).orElseThrow(
                            () -> new NotFoundException(
                                    String.format(USER_NOT_FOUND,senderUsername)
                            )
                    );

            var receiver = userRepository.findByUsername(receiverUsername)
                    .filter(UserEntity::isActive).orElseThrow(
                            () -> new NotFoundException(
                                    String.format(USER_NOT_FOUND,receiverUsername)
                            )
                    );

            MessageDto sendingMessage = createMessageDto(
                    messageRepository.saveAndFlush(
                        MessageEntity.builder()
                                .sender(sender)
                                .receiver(receiver)
                                .text(message.getPayload().toString())
                                .build()
                    )
            );
            if(userSessions.containsKey(receiverUsername))
                userSessions.get(receiverUsername).get(senderUsername)
                        .sendMessage(new TextMessage(objectMapper.writeValueAsString(sendingMessage)));
        } catch (NotFoundException e){
            session.sendMessage(new TextMessage(
                    objectMapper.writeValueAsString(
                            ErrorDto.builder()
                                    .error("Not Found")
                                    .errorDescription(e.getMessage())
                                    .build())
            ));
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        userSessions.remove(userRepository.findByUsername(session.getPrincipal().getName()).orElseThrow().getUsername());
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close();
    }
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    private String getUsername(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
    private MessageDto createMessageDto(MessageEntity message){
        return MessageDto.builder()
                .message(message.getText())
                .sender(message.getSender().getUsername())
                .receiver(message.getReceiver().getUsername())
                .sentAt(message.getSentAt())
                .build();
    }
}

