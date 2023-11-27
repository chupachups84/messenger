package com.chernyshev.messenger.users.controllers;

import com.chernyshev.messenger.exception.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.users.dtos.AuthenticationRequest;
import com.chernyshev.messenger.users.dtos.RegisterRequest;
import com.chernyshev.messenger.users.dtos.TokensResponse;
import com.chernyshev.messenger.users.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    @PostMapping("/register")
    public ResponseEntity<TokensResponse> register(@RequestBody @Valid RegisterRequest request) throws IllegalStateException {
        TokensResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<TokensResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) throws UserDeactivatedException {
            TokensResponse response = authenticationService.login(request);
            return  ResponseEntity.ok(response);
    }
    @GetMapping("/confirm/{emailConfirmationToken}")
    public ResponseEntity<String> confirm(@PathVariable String emailConfirmationToken) throws InvalidTokenException {
        return authenticationService.confirmation(emailConfirmationToken);
    }
    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request,response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) throws UserDeactivatedException {
        return authenticationService.logout(request);
    }
}
