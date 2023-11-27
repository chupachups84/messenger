package com.chernyshev.messenger.messages.services;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.friends.services.FriendService;
import com.chernyshev.messenger.messages.dtos.MessageResponse;
import com.chernyshev.messenger.messages.models.MessageEntity;
import com.chernyshev.messenger.messages.repositories.MessageRepository;
import com.chernyshev.messenger.users.models.UserEntity;
import com.chernyshev.messenger.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;
    public MessageResponse sendMessage(String senderName, Long receiverId, String text) throws FriendshipException,IllegalStateException {
        UserEntity sender = userRepository.findByUsername(senderName).orElseThrow();
        UserEntity receiver = userRepository.findById(receiverId).orElse(null);
        if(receiver==null||!receiver.isActive()) throw new IllegalStateException("Пользователь не найден");
        if (receiver.isPrivateProfile()&&!friendService.areFriends(sender,receiver))
            throw new FriendshipException("Не можете отправить сообщение пользователю");
        var message= MessageEntity.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .text(text)
                        .build();
        messageRepository.save(message);
        return MessageResponse.builder()
                .sentAt(message.getSentAt())
                .receiver(message.getReceiver().getFirstname())
                .sender(message.getSender().getFirstname())
                .text(message.getText())
                .build();
    }
    public List<MessageResponse> getMessageHistory(String senderName, Long receiverId) throws IllegalStateException{
        UserEntity sender = userRepository.findByUsername(senderName).orElseThrow();
        UserEntity receiver = userRepository.findById(receiverId).orElse(null);
        if(receiver==null||!receiver.isActive()) throw new IllegalStateException("Пользователь не найден");
        return messageRepository.findBySenderAndReceiver(sender, receiver).stream()
                .map((message)-> MessageResponse.builder()
                        .sender(message.getSender().getFirstname())
                        .receiver(message.getReceiver().getFirstname())
                        .text(message.getText())
                        .sentAt(message.getSentAt())
                        .build()).collect(Collectors.toList());

    }

    public void deleteMessageHistory(String senderName, Long receiverId) {
        UserEntity sender = userRepository.findByUsername(senderName).orElseThrow();
        UserEntity receiver = userRepository.findById(receiverId).orElse(null);
        if(receiver==null||!receiver.isActive()) throw new IllegalStateException("Пользователь не найден");
        messageRepository.deleteAll(messageRepository.findBySenderAndReceiver(sender, receiver));
    }
}
