package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
import com.chernyshev.messenger.api.exceptions.*;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FriendController {
    private final UserRepository repository;
    private static final String USER_NOT_FOUND="Пользователь %s не найден";
    public static final String GET_FRIEND_LIST="/api/v1/friends";
    public static final String ADD_FRIEND_TO_LIST="/api/v1/friends/add";
    @GetMapping(GET_FRIEND_LIST)
    public ResponseEntity<List<InfoDto>> getFriendList(
            Principal principal,
            @RequestParam(value = "username",required = false) Optional<String> optionalUsername){

        final List<InfoDto> infoDtoList = new ArrayList<>();
        String targetUsername = optionalUsername.orElse(principal.getName());

        UserEntity userEntity = repository.findByUsernameAndActive(targetUsername,true)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_NOT_FOUND, targetUsername)));

        if (!principal.getName().equals(targetUsername) && userEntity.isFriendsListHidden()) {
            throw new FriendsListHiddenException(String.format("Пользователь %s скрыл список друзей", targetUsername));
        }

        repository.getFriendList(targetUsername).ifPresent(
                friendList -> infoDtoList.addAll(friendList.stream().map(this::createInfoDto).toList())
        );
        return ResponseEntity.ok().body(infoDtoList);
    }
    @PostMapping(ADD_FRIEND_TO_LIST)
    public ResponseEntity<String> addToFriends(Principal principal, @RequestParam(value = "username") String username){
        if(principal.getName().equals(username))
            throw new FriendRequestException("Нельзя добавить себя в друзья");

        var user1=repository.findByUsernameAndActive(principal.getName(),true)
                .orElseThrow(
                        ()->new UserNotFoundException(String.format(USER_NOT_FOUND,principal.getName()))
                );
        var user2= repository.findByUsernameAndActive(username,true)
                .orElseThrow(
                        ()->new UserNotFoundException(String.format(USER_NOT_FOUND,username))
                );
        if(repository.areFriends(principal.getName(), username))
            throw new FriendRequestException("Пользователь уже в друзьях");
        user1.getFriends().add(user2);
        user2.getFriends().add(user1);
        repository.saveAndFlush(user1);
        repository.saveAndFlush(user2);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(String.format("{\"message\":\"Пользователь %s добавлен в друзья\"}",username));
    }
    private InfoDto createInfoDto(UserEntity user) {
        return InfoDto.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus())
                .build();
    }
}
