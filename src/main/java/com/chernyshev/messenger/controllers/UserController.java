package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.*;
import com.chernyshev.messenger.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    public static final  String USER ="/api/v1/users/{username}";
    public static final  String USER_CHANGE_PASSWORD ="/api/v1/users/{username}/password-change";
    public static final String USER_FRIENDS="/api/v1/users/{username}/friends";

    @GetMapping(USER)
    public ResponseEntity<UserDto> getUserInfo(@PathVariable String username){
        return userService.getUserInfo(username);
    }
    @PatchMapping(USER)
    public ResponseEntity<TokenDto> changeUserInfo(
            Principal principal,@PathVariable String username,@RequestBody UserDto userDto
    ){
        return userService.changeUserInfo(principal,username,userDto);
    }

    @PatchMapping(USER_CHANGE_PASSWORD)
    public ResponseEntity<String> changePassword(
            Principal principal,@PathVariable String username,@RequestBody PasswordDto request
    ) {
        return userService.changeUserPassword(principal,username,request);
    }
    @DeleteMapping(USER)
    public ResponseEntity<RecoverTokenDto> deleteUser(Principal principal,@PathVariable String username) {
        return userService.deleteUser(principal,username);
    }
    @PutMapping(USER)
    public ResponseEntity<TokenDto> recoverUser(@RequestBody RecoverTokenDto token,@PathVariable String username) {
        return userService.recoverUser(token,username);
    }
    @GetMapping(USER_FRIENDS)
    public ResponseEntity<List<UserDto>> getFriendList(Principal principal, @PathVariable String username){
        return userService.getFriendList(principal,username);
    }
    @PostMapping(USER_FRIENDS)
    public ResponseEntity<String> addToFriends(Principal principal, @PathVariable String username){
        return userService.addFriend(principal,username);
    }

}
