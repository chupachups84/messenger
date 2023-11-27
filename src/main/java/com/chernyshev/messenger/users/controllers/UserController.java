package com.chernyshev.messenger.users.controllers;

import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.users.dtos.*;
import com.chernyshev.messenger.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    @GetMapping()
    public ResponseEntity<InfoResponse> info(Principal principal) throws UserDeactivatedException {
        InfoResponse response = userService.info(principal.getName());
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/edit")
    public ResponseEntity<String> edit(@RequestBody @Valid InfoRequest request, Principal principal)
            throws UserDeactivatedException {
        userService.edit(request,principal.getName());
        return ResponseEntity.ok().build();
    }
    @PostMapping("/change-username")
    public ResponseEntity<TokensResponse> changeUsername(@Valid @RequestBody UsernameRequest request, Principal principal)
            throws IllegalStateException, UserDeactivatedException {
        TokensResponse response = userService.changeUsername(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/change-email")
    public ResponseEntity<TokensResponse> changeEmail(@Valid @RequestBody EmailRequest request, Principal principal)
            throws UserDeactivatedException, IllegalStateException {
        TokensResponse response = userService.changeEmail(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/change-password")
    public ResponseEntity<TokensResponse> changePassword(@RequestBody @Valid PasswordRequest request, Principal principal)
            throws UserDeactivatedException, IllegalStateException {
        TokensResponse response = userService.changePassword(request,principal.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/delete")
    public ResponseEntity<TokensResponse> delete(Principal principal) throws UserDeactivatedException {
        TokensResponse response = userService.delete(principal.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/recover")
    public ResponseEntity<TokensResponse> recover(Principal principal) {
        TokensResponse response = userService.recover(principal.getName());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/change-profile-privacy")
    public ResponseEntity<String> setPrivateProfile(Principal principal) throws UserDeactivatedException {
        userService.changeProfilePrivacy(principal.getName());
        return ResponseEntity.ok().build();
    }

}
