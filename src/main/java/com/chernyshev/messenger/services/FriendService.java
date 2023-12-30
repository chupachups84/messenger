package com.chernyshev.messenger.services;

import com.chernyshev.messenger.exception.myExceptions.FriendshipException;
import com.chernyshev.messenger.dtos.FriendResponse;
import com.chernyshev.messenger.models.FriendEntity;
import com.chernyshev.messenger.repositories.FriendRepository;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    public boolean areFriends(UserEntity user, UserEntity friend){
        return friendRepository.existsByUserAndFriend(user,friend)&& friendRepository.existsByUserAndFriend(friend,user);
    }

    public void sendFriendRequest(String name, Long friendId) {
        var user = userRepository.findByUsername(name).orElseThrow();
        var friend = userRepository.findById(friendId).orElse(null);
        if(friend==null||!friend.isActive()) throw new IllegalStateException("Пользователь не найден");
        if(name.equals(friend.getUsername())) throw new IllegalStateException("Нельзя отправить запрос себе");
        if(friendRepository.existsByUserAndFriend(user,friend)) throw new IllegalStateException("Вы уж отправили заявку этому пользователю");
        var friendEntity = FriendEntity.builder().user(user).friend(friend).build();
        friendRepository.save(friendEntity);
    }

    public List<FriendResponse> getFriends(String name){
        var user = userRepository.findByUsername(name).orElseThrow();
        List<FriendEntity> userFriends = friendRepository.findAllByUser(user);
        return getFilteredFriendshipResponses(user, userFriends);
    }
    public List<FriendResponse> getUserFriends(Long userId,String username ){
        var friend = userRepository.findById(userId).orElse(null);
        if(friend==null|| !friend.isActive()) throw new IllegalStateException("Пользователь не найден");
        var user = userRepository.findByUsername(username).orElseThrow();
        if(user.getUsername().equals(friend.getUsername())) return getFriends(friend.getUsername());
        if(friend.isPrivateProfile()&&!areFriends(user,friend))
            throw new FriendshipException("Вы не можете просматривать список друзей этого пользователя, так как у него установлены настройки приватности профиля");
        List<FriendEntity> userFriends = friendRepository.findAllByUser(friend);
        return getFilteredFriendshipResponses(friend, userFriends);
    }
    private List<FriendResponse> getFilteredFriendshipResponses(UserEntity user, List<FriendEntity> userFriends) {
        return userFriends.stream()
                .map(FriendEntity::getFriend)
                .filter(UserEntity::isActive)
                .filter(friend -> areFriends(user, friend))
                .map(friend->new FriendResponse(
                        friend.getLastname(),
                        friend.getFirstname(),
                        friend.getBio(),
                        friend.getStatus(),
                        friend.getAvatarUrl()
                ))
                .collect(Collectors.toList());
    }

    public void deleteFriend(String name, Long id) {
        var user1 = userRepository.findByUsername(name).orElseThrow();
        var user2 = userRepository.findById(id).orElse(null);
        if(user2==null||!user2.isActive()) throw new IllegalStateException("Пользователь не найден");
        var friendEntity =friendRepository.findByUserAndFriend(user1,user2).orElseThrow();
        friendRepository.delete(friendEntity);
    }
}
