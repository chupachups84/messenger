package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.AuthenticationDto;
import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.exception.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.services.AuthenticationService;
import com.chernyshev.messenger.dtos.RegisterDto;
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
    public final String REGISTER ="/register";
    public final String LOGIN ="/login";
    public final String EMAIL_CONFIRMATION ="/confirm";
    public final String REFRESH_TOKEN = "/refresh-token";
    public final String LOGOUT ="/logout";

    @PostMapping(REGISTER)
    public ResponseEntity<TokenDto> register(@RequestBody @Valid RegisterDto request) {
        TokenDto response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping(LOGIN)
    public ResponseEntity<TokenDto> authenticate(@RequestBody @Valid AuthenticationDto request) {
            TokenDto response = authenticationService.login(request);
            return  ResponseEntity.ok(response);
    }
    @PostMapping(EMAIL_CONFIRMATION)
    public ResponseEntity<String> confirm(@RequestParam String token) throws InvalidTokenException {
        return authenticationService.confirmation(token);
    }
    @PostMapping(REFRESH_TOKEN)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request,response);
    }
    @PostMapping(LOGOUT)
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return authenticationService.logout(request);
    }
}
