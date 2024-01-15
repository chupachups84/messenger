package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.*;
import com.chernyshev.messenger.api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    public static final  String GET_USER_INFO ="/api/v1/user/{username}";
    public static final  String EDIT_USER_INFO ="/api/v1/user/{username}";
    public static final  String CHANGE_USER_PASSWORD ="/api/v1/user/{username}/password-change";
    public static final  String DELETE_USER_ACCOUNT ="/api/v1/user/{username}";
    public static final  String RECOVER_USER_ACCOUNT ="/api/v1/user/{username}";

    @GetMapping(GET_USER_INFO)
    public ResponseEntity<InfoDto> info(@PathVariable String username){
        return userService.getUserInfo(username);
    }
    @PatchMapping(EDIT_USER_INFO)
    public ResponseEntity<String> edit(
            Principal principal,@PathVariable String username,@RequestBody EditDto editDto
    ){
        return userService.changeUserInfo(principal,username,editDto);
    }

    @PatchMapping(CHANGE_USER_PASSWORD)
    public ResponseEntity<String> changePassword(
            Principal principal,@PathVariable String username,@RequestBody PasswordDto request
    ) {
        return userService.changeUserPassword(principal,username,request);
    }
    @DeleteMapping(DELETE_USER_ACCOUNT)
    public ResponseEntity<RecoverTokenDto> delete(Principal principal,@PathVariable String username) {
        return userService.deleteUser(principal,username);
    }
    @PutMapping(RECOVER_USER_ACCOUNT)
    public ResponseEntity<TokenDto> recover(@RequestBody RecoverTokenDto token,@PathVariable String username) {
        return userService.recoverUser(token,username);
    }

}
