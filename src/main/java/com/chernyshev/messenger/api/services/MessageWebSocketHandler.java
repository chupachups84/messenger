package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.dtos.ErrorDto;
import com.chernyshev.messenger.api.dtos.MessageDto;
import com.chernyshev.messenger.api.exceptions.InternalServerException;
import com.chernyshev.messenger.api.exceptions.MessageFriendOnlyException;
import com.chernyshev.messenger.api.exceptions.UserNotFoundException;
import com.chernyshev.messenger.store.models.MessageEntity;
import com.chernyshev.messenger.store.repositories.FriendRepository;
import com.chernyshev.messenger.store.repositories.MessageRepository;
import com.chernyshev.messenger.store.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketHandler implements WebSocketHandler {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;
    private final Map<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String USER_NOT_FOUND ="Пользователь %s не найден";
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        try {
            String senderUsername =session.getPrincipal().getName() ;
            String receiverUsername = getUsernameFromPath(session.getUri().getPath());
            var sender = userRepository.findByUsername(senderUsername)
                    .orElseThrow(
                            () -> new UserNotFoundException(String.format(USER_NOT_FOUND,senderUsername))
                    );
            var receiver = userRepository.findByUsernameAndActive(receiverUsername,true)
                    .orElseThrow(
                            () -> new UserNotFoundException(String.format(USER_NOT_FOUND,receiverUsername))
                    );

            if(receiver.isReceiveMessagesFriendOnly()&&!friendRepository.existsByUser1AndUser2(sender,receiver))
                throw new MessageFriendOnlyException("Пользователь ограничил получение сообщений своим кругом друзей");

            if (!userSessions.containsKey(senderUsername))
                userSessions.put(senderUsername, new ConcurrentHashMap<>());
            userSessions.get(senderUsername).put(receiverUsername,session);

            messageRepository.findBySenderAndReceiver(sender, receiver).forEach(
                    message -> {
                        try {
                            session.sendMessage(
                                    new TextMessage(
                                            objectMapper.writeValueAsString(createMessageDto(message))
                                    )
                            );
                        } catch (IOException e) {
                            throw new InternalServerException("Возникла ошибка в момент отправки сообщения");
                        }
                    }
            );
        } catch (UserNotFoundException | MessageFriendOnlyException | InternalServerException e){
            String errorName;
            if(e instanceof MessageFriendOnlyException)
                errorName="Forbidden";
            else if(e instanceof UserNotFoundException)
                errorName="Not Found";
            else
                errorName = "Internal Server";


            session.sendMessage(
                    new TextMessage(
                            objectMapper.writeValueAsString(
                                    ErrorDto.builder()
                                            .error(errorName)
                                            .errorDescription(e.getMessage())
                                            .build()
                            )
                    )
            );
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {

            String senderUsername = session.getPrincipal().getName();
            String receiverUsername = getUsernameFromPath(session.getUri().getPath());
            var sender = userRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND,senderUsername)));

            var receiver = userRepository.findByUsernameAndActive(receiverUsername,true)
                    .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND,receiverUsername)));

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
        } catch (UserNotFoundException e){
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
        userSessions.remove(session.getPrincipal().getName());
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close();
    }
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    private String getUsernameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
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

