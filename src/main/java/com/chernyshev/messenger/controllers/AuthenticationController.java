package com.chernyshev.messenger.controllers;

import com.chernyshev.messenger.dtos.AuthenticationDto;
import com.chernyshev.messenger.dtos.RegisterDto;
import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserService userService;
    public static final String REGISTER ="/api/v1/auth/register";
    public static final String LOGIN ="/api/v1/auth/login";
    public static final String EMAIL_CONFIRMATION ="/api/v1/auth/confirmation";
    public static final String REFRESH_TOKEN = "/api/v1/auth/token-refresh";
    public static final String LOGOUT ="/api/v1/auth/logout";

    @PostMapping(REGISTER)
    public ResponseEntity<TokenDto> signUp(@RequestBody RegisterDto request) {
        return userService.signUp(request);
    }
    @PostMapping(LOGIN)
    public ResponseEntity<TokenDto> signIn(@RequestBody AuthenticationDto request) {
        return userService.signIn(request);
    }
    @GetMapping(EMAIL_CONFIRMATION)
    public ResponseEntity<String> confirm(@RequestParam String confirmationToken) {
        return userService.emailConfirm(confirmationToken);
    }
    @PutMapping(REFRESH_TOKEN)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response)  {
        userService.tokenRefresh(request,response);
    }
    @PostMapping(LOGOUT)
    public ResponseEntity<String> signOut(HttpServletRequest request) {
       return userService.signOut(request);
    }
}
