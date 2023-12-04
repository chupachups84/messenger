package com.chernyshev.messenger.websocket;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.messages.dtos.MessageResponse;
import com.chernyshev.messenger.messages.services.MessageService;
import com.chernyshev.messenger.users.repositories.UserRepository;
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
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        String[] segments = Objects.requireNonNull(session.getUri()).getPath().split("/");
//        Long receiverId = Long.valueOf(segments[segments.length - 1]);
//        UserEntity receiver = userRepository.findById(receiverId).orElseThrow();
//        session.getAttributes().put("receiver", receiver.getUsername());
//        String sender = Objects.requireNonNull(session.getPrincipal()).getName();
//
//        if (!userSessions.containsKey(sender)) {
//            userSessions.put(sender, new HashMap<>());
//        }
//        userSessions.get(sender).put(receiver.getUsername(), session);
//
//        List<MessageResponse> messageHistory = messageService.getMessageHistory(sender, receiverId);
//        for (MessageResponse messageResponse : messageHistory) {
//            session.sendMessage(new TextMessage(messageResponse.getSentAt()
//                    + " " + messageResponse.getReceiver() + " : " + messageResponse.getText()));
//        }
//    }
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String sender = session.getPrincipal().getName();
//        String receiver = (String) session.getAttributes().get("receiver");
//
//        try {
//            WebSocketSession receiverSession = userSessions.get(receiver).get(sender);
//            MessageResponse newMessage = messageService.sendMessage(sender, userRepository.findByUsername(receiver).orElse(null).getId(), message.getPayload());
//            if (receiverSession != null) {
//                receiverSession.sendMessage(new TextMessage(newMessage.getSentAt() + " " + newMessage.getSender() + " : " + newMessage.getText()));
//            }
//        } catch (FriendshipException e) {
//            session.sendMessage(new TextMessage("Не удалось отправить сообщение, так как пользователь использует приватные настройки аккаунта"));
//        } catch (UsernameNotFoundException e){
//            session.sendMessage(new TextMessage("Не удалось отправить сообщение, пользователь "+ receiver+" не существует"));
//        } catch (Exception e){
//            session.sendMessage(new TextMessage("Возникла ошибка при отправке сообщения"));
//        }
//    }
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        String sender = session.getPrincipal().getName();
//        String receiver = (String) session.getAttributes().get("receiver");
//
//        if (userSessions.containsKey(sender)) {
//            userSessions.get(sender).remove(receiver);
//            if (userSessions.get(sender).isEmpty()) {
//                userSessions.remove(sender);
//            }
//        }
//    }
}

