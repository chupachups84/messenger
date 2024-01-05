package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
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
@RequestMapping("/api/v1/friends")
public class FriendController {
    private final UserRepository repository;

    @GetMapping()
    public ResponseEntity<List<InfoDto>> getFriendList(Principal principal, @RequestParam(value = "username",required = false) Optional<String> optionalUsername){
        final List<InfoDto> infoDtoList = new ArrayList<>();
        optionalUsername.ifPresentOrElse(
                s -> repository.findByUsername(s).filter(UserEntity::isActive).ifPresentOrElse(
                        userEntity -> {
                            if(userEntity.isFriendsListHidden())
                                throw new ForbiddenException(
                                        String.format("Пользователь \"%s\" скрыл список друзей",s)
                                );
                            repository.getFriends(s)
                                    .ifPresent(
                                            friendList-> infoDtoList.addAll(
                                                    friendList.stream().map(
                                                            friend-> InfoDto.builder()
                                                                    .firstname(friend.getFirstname())
                                                                    .lastname(friend.getLastname())
                                                                    .username(friend.getUsername())
                                                                    .email(friend.getEmail())
                                                                    .avatarUrl(friend.getAvatarUrl())
                                                                    .bio(friend.getBio())
                                                                    .status(friend.getStatus()).build()).toList()
                                            )
                                    );
                        },
                        ()->{
                            throw new NotFoundException(String.format("Пользователь с \"%s\" не найден",s));
                        }
                ),
                ()-> repository.getFriends(principal.getName())
                        .ifPresent(
                                friendList-> infoDtoList.addAll(
                                        friendList.stream().map(
                                            friend-> InfoDto.builder()
                                                    .firstname(friend.getFirstname())
                                                    .lastname(friend.getLastname())
                                                    .username(friend.getUsername())
                                                    .email(friend.getEmail())
                                                    .avatarUrl(friend.getAvatarUrl())
                                                    .bio(friend.getBio())
                                                    .status(friend.getStatus()).build()).toList()
                                )
                        )
        );
        return ResponseEntity.ok().body(infoDtoList);
    }
    @PostMapping()
    public ResponseEntity<Object> addToFriends(Principal principal, @RequestParam(value = "username",required = true) Optional<String> optionalUsername){
        return ResponseEntity.ok();
    }
}
