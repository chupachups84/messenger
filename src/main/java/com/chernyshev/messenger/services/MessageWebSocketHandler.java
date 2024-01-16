package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.ErrorDto;
import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.exceptions.custom.InternalServerException;
import com.chernyshev.messenger.exceptions.custom.MessageFriendOnlyException;
import com.chernyshev.messenger.exceptions.custom.UserNotFoundException;
import com.chernyshev.messenger.repositories.UserRepository;
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
    private final MessageService messageService;
    private final UserRepository userRepository;

    private final Map<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String USER_NOT_FOUND ="Пользователь %s не найден";
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        try {

            String senderUsername =session.getPrincipal().getName() ;
            String receiverUsername = getUsernameFromPath(session.getUri().getPath());

            var receiver = userRepository.findByUsernameAndActive(receiverUsername,true)
                    .orElseThrow(
                            () -> new UserNotFoundException(String.format(USER_NOT_FOUND,receiverUsername))
                    );

            if(receiver.isReceiveMessagesFriendOnly()&&!messageService.areFriends(senderUsername,receiverUsername))
                throw new MessageFriendOnlyException("Пользователь ограничил получение сообщений своим кругом друзей");

            if (!userSessions.containsKey(senderUsername))
                userSessions.put(senderUsername, new ConcurrentHashMap<>());
            userSessions.get(senderUsername).put(receiverUsername,session);

            messageService.getMessageHistory(senderUsername, receiverUsername).forEach(
                    message -> {
                        try {
                            session.sendMessage(
                                    new TextMessage(
                                            objectMapper.writeValueAsString(message)
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

            MessageDto sendingMessage=messageService.sendMessage(sender,receiver,message.getPayload().toString());
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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
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
}

