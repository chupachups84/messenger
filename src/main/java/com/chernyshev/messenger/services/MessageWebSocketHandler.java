package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketHandler implements WebSocketHandler {
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final Map<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String USER_NOT_FOUND = "User %s not found";
    private static final String RECEIPT_MESSAGES_FRIENDS_ONLY
            = "The user has limited the receipt of messages to his circle of friends";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        String senderUsername = Objects.requireNonNull(session.getPrincipal()).getName();
        String receiverUsername = getUsernameFromPath(Objects.requireNonNull(session.getUri()).getPath());
        final var receiver
                = userRepository.findByUsername(receiverUsername).filter(UserEntity::isEnabled).orElse(null);
        if (receiver == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason(String.format(USER_NOT_FOUND, receiverUsername)));
            return;
        }

        if (receiver.isReceiptMessagesFriendOnly() && !messageService.areFriends(senderUsername, receiverUsername)) {
            session.close(CloseStatus.PROTOCOL_ERROR.withReason(RECEIPT_MESSAGES_FRIENDS_ONLY));
        }
        userSessions.computeIfAbsent(senderUsername, k -> new ConcurrentHashMap<>()).put(receiverUsername, session);
        log.info(userSessions.toString());

        List<MessageDto> messages = messageService.getMessageHistory(senderUsername, receiverUsername);
        for (MessageDto message : messages) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        }


    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String senderUsername = Objects.requireNonNull(session.getPrincipal()).getName();
        String receiverUsername = getUsernameFromPath(Objects.requireNonNull(session.getUri()).getPath());
        final var sender = userRepository.findByUsername(senderUsername).orElse(null);

        if (sender == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason(String.format(USER_NOT_FOUND, senderUsername)));
        }

        var receiver = userRepository.findByUsername(receiverUsername).filter(UserEntity::isEnabled).orElse(null);

        if (receiver == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason(String.format(USER_NOT_FOUND, receiverUsername)));
        }
        MessageDto sendingMessage = messageService.sendMessage(sender, receiver, message.getPayload().toString());
        if (userSessions.containsKey(receiverUsername) && (userSessions.get(receiverUsername).containsKey(senderUsername))) {
            userSessions
                    .get(receiverUsername)
                    .get(senderUsername)
                    .sendMessage(new TextMessage(objectMapper.writeValueAsString(sendingMessage)));

        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        userSessions.remove(Objects.requireNonNull(session.getPrincipal()).getName());
        log.info("Session:{} closed with closeStatus: {}", session, closeStatus.toString());
        log.info(userSessions.toString());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error {} occurred due session {}", exception.getMessage(), session.toString());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUsernameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}

