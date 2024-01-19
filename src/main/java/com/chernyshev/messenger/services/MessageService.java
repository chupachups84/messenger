package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.models.MessageEntity;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.models.enums.StatusType;
import com.chernyshev.messenger.repositories.FriendRepository;
import com.chernyshev.messenger.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;

    @Transactional(readOnly = true)
    public boolean areFriends(String username1, String username2) {
        return friendRepository.findByUsername1AndUsername2(username1, username2)
                .filter(friend -> friend.getStatusType().equals(StatusType.APPROVED)).isPresent();
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessageHistory(String username1, String username2) {
        return messageRepository.findBySenderAndReceiver(username1, username2)
                .stream().map(
                        message -> MessageDto.builder()
                                .message(message.getText())
                                .sentAt(message.getSentAt())
                                .sender(username1)
                                .receiver(username2)
                                .build()
                ).toList();
    }

    @Transactional()
    public MessageDto sendMessage(UserEntity sender, UserEntity receiver, String text) {
        return convertToMessageDto(messageRepository.saveAndFlush(
                        MessageEntity.builder()
                                .text(text)
                                .sender(sender)
                                .receiver(receiver)
                                .build()
                )
        );
    }

    public MessageDto convertToMessageDto(MessageEntity message) {
        return MessageDto.builder()
                .receiver(message.getReceiver().getUsername())
                .sender(message.getSender().getUsername())
                .message(message.getText())
                .sentAt(message.getSentAt())
                .build();
    }

}
