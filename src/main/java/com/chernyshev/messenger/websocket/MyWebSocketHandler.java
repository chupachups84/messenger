package com.chernyshev.messenger.websocket;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.messages.dtos.MessageResponse;
import com.chernyshev.messenger.messages.services.MessageService;
import com.chernyshev.messenger.users.models.UserEntity;
import com.chernyshev.messenger.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final Map<String, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String[] segments = Objects.requireNonNull(session.getUri()).getPath().split("/");
        Long receiverId = Long.valueOf(segments[segments.length - 1]);
        UserEntity receiver = userRepository.findById(receiverId).orElseThrow();
        session.getAttributes().put("receiver", receiver.getUsername());
        String sender = Objects.requireNonNull(session.getPrincipal()).getName();

        if (!userSessions.containsKey(sender)) {
            userSessions.put(sender, new HashMap<>());
        }
        userSessions.get(sender).put(receiver.getUsername(), session);

        List<MessageResponse> messageHistory = messageService.getMessageHistory(sender, receiverId);
        for (MessageResponse messageResponse : messageHistory) {
            session.sendMessage(new TextMessage(messageResponse.getSentAt()
                    + " " + messageResponse.getReceiver() + " : " + messageResponse.getText()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sender = session.getPrincipal().getName();
        String receiver = (String) session.getAttributes().get("receiver");

        try {
            WebSocketSession receiverSession = userSessions.get(receiver).get(sender);
            MessageResponse newMessage = messageService.sendMessage(sender, userRepository.findByUsername(receiver).orElse(null).getId(), message.getPayload());
            if (receiverSession != null) {
                receiverSession.sendMessage(new TextMessage(newMessage.getSentAt() + " " + newMessage.getSender() + " : " + newMessage.getText()));
            }
        } catch (FriendshipException e) {
            session.sendMessage(new TextMessage("Не удалось отправить сообщение, так как пользователь использует приватные настройки аккаунта"));
        } catch (UsernameNotFoundException e){
            session.sendMessage(new TextMessage("Не удалось отправить сообщение, пользователь "+ receiver+" не существует"));
        } catch (Exception e){
            session.sendMessage(new TextMessage("Возникла ошибка при отправке сообщения"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sender = session.getPrincipal().getName();
        String receiver = (String) session.getAttributes().get("receiver");

        if (userSessions.containsKey(sender)) {
            userSessions.get(sender).remove(receiver);
            if (userSessions.get(sender).isEmpty()) {
                userSessions.remove(sender);
            }
        }
    }
}

