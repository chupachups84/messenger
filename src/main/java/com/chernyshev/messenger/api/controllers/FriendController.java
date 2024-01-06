package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
import com.chernyshev.messenger.api.exceptions.BadRequestException;
import com.chernyshev.messenger.api.exceptions.ForbiddenException;
import com.chernyshev.messenger.api.exceptions.NotFoundException;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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

        UserEntity userEntity = repository.findByUsername(targetUsername)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new NotFoundException(String.format(USER_NOT_FOUND, targetUsername)));

        if (!principal.getName().equals(targetUsername) && userEntity.isFriendsListHidden()) {
            throw new ForbiddenException(String.format("Пользователь %s скрыл список друзей", targetUsername));
        }

        repository.getFriendList(targetUsername).ifPresent(
                friendList -> infoDtoList.addAll(friendList.stream().map(this::createInfoDto).toList())
        );
        return ResponseEntity.ok().body(infoDtoList);
    }
    @PostMapping(ADD_FRIEND_TO_LIST)
    public ResponseEntity<String> addToFriends(Principal principal, @RequestParam(value = "username") String username){
        var user1=repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(
                        ()->new NotFoundException(String.format(USER_NOT_FOUND,principal.getName()))
                );
        var user2= repository.findByUsername(username).filter(UserEntity::isActive)
                .orElseThrow(
                        ()->new NotFoundException(String.format(USER_NOT_FOUND,username))
                );
        if(principal.getName().equals(username))
            throw new BadRequestException("Нельзя добавить себя в друзья");
        if(repository.findFriendshipByUsername1AndUsername2(principal.getName(), username))
            throw new BadRequestException("Пользователь уже в друзьях");
        user1.getFriends().add(user2);
        repository.saveAndFlush(user1);
        return ResponseEntity.ok(String.format("Пользователь %s добавлен в друзья",username));
    }
    private InfoDto createInfoDto(UserEntity friend) {
        return InfoDto.builder()
                .firstname(friend.getFirstname())
                .lastname(friend.getLastname())
                .username(friend.getUsername())
                .email(friend.getEmail())
                .avatarUrl(friend.getAvatarUrl())
                .bio(friend.getBio())
                .status(friend.getStatus())
                .build();
    }
}
