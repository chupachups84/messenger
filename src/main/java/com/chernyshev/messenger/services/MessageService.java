package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.MessageDto;
import com.chernyshev.messenger.dtos.TextMessageDto;
import com.chernyshev.messenger.exceptions.custom.MessageBadRequestException;
import com.chernyshev.messenger.exceptions.custom.MessageFriendOnlyException;
import com.chernyshev.messenger.exceptions.custom.UserNotFoundException;
import com.chernyshev.messenger.models.MessageEntity;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.models.enums.StatusType;
import com.chernyshev.messenger.repositories.FriendRepository;
import com.chernyshev.messenger.repositories.MessageRepository;
import com.chernyshev.messenger.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final FriendRepository friendRepository;

    public static final String FRIEND_EXC = "This user has limited the receipt of the message to a circle of friends";
    public static final String EMPTY_MESSAGE = "The message is empty";
    public static final String SUCCESS = "Message successfully send";

    @Transactional(readOnly = true)
    public List<MessageDto> getMessageHistory(String username1, String username2) {
        if (userRepository.findByUsername(username2).isEmpty()) {
            throw new UserNotFoundException(UserService.NOT_FOUND_MESSAGE);
        }
        return messageRepository.findBySenderAndReceiver(username1, username2)
                .stream()
                .map(this::convertToMessageDto)
                .toList();

    }

    @Transactional()
    public String sendMessage(String username1, String username2, TextMessageDto text) {
        var sender = userRepository.findByUsername(username1).orElseThrow(
                () -> new UserNotFoundException(UserService.NOT_FOUND_MESSAGE)
        );
        var receiver = userRepository.findByUsername(username2).filter(UserEntity::isEnabled).orElseThrow(
                () -> new UserNotFoundException(UserService.NOT_FOUND_MESSAGE)
        );
        if (text.getText().trim().isEmpty()) {
            throw new MessageBadRequestException(EMPTY_MESSAGE);
        }
        if (receiver.isReceiptMessagesFriendOnly() && !areFriends(username1, username2)) {
            throw new MessageFriendOnlyException(FRIEND_EXC);
        }

        messageRepository.saveAndFlush(
                MessageEntity.builder()
                        .text(text.getText())
                        .sender(sender)
                        .receiver(receiver)
                        .build()
        );
        return SUCCESS;
    }

    private boolean areFriends(String username1, String username2) {
        return friendRepository.findByUsername1AndUsername2(username1, username2)
                .filter(friendship -> friendship.getStatusType().equals(StatusType.APPROVED)).isPresent();
    }

    private MessageDto convertToMessageDto(MessageEntity message) {
        return MessageDto.builder()
                .receiver(message.getReceiver().getUsername())
                .sender(message.getSender().getUsername())
                .message(message.getText())
                .sentAt(message.getSentAt())
                .build();
    }


}
