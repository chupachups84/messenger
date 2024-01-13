package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.AuthenticationDto;
import com.chernyshev.messenger.api.dtos.RegisterDto;
import com.chernyshev.messenger.api.dtos.TokenDto;
import com.chernyshev.messenger.api.exceptions.*;
import com.chernyshev.messenger.api.services.EmailService;
import com.chernyshev.messenger.api.services.JwtService;
import com.chernyshev.messenger.api.services.TokenService;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.TokenRepository;
import com.chernyshev.messenger.store.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    public static final String REGISTER ="/api/v1/auth/register";
    public static final String LOGIN ="/api/v1/auth/login";
    public static final String EMAIL_CONFIRMATION ="/api/v1/auth/email-confirm";
    public static final String REFRESH_TOKEN = "/api/v1/auth/token-refresh";
    public static final String LOGOUT ="/api/v1/auth/logout";

    @PostMapping(REGISTER)
    public ResponseEntity<TokenDto> signUp(@RequestBody RegisterDto request) {
        repository.findByUsername(request.getUsername())
                .ifPresent(user->{
                    throw new UsernameAlreadyExistException(
                            String.format("Пользователь %s уже существует",request.getUsername())
                    );
                });
        repository.findByEmail(request.getEmail())
                .ifPresent(user->{
                    throw new EmailAlreadyExistException(String.format("Почта %s занята",request.getEmail()));
                });
        String emailToken = UUID.randomUUID().toString();
        emailService.sendEmailConfirmationEmail(request.getEmail(), emailToken);
        return ResponseEntity.ok(
                tokenService
                        .getTokenDto(
                                repository.saveAndFlush(
                                        UserEntity.builder()
                                                .firstname(request.getFirstname())
                                                .lastname(request.getLastname())
                                                .email(request.getEmail())
                                                .username(request.getUsername())
                                                .password(passwordEncoder.encode(request.getPassword()))
                                                .emailToken(emailToken)
                                                .build()
                                )
                        )
        );
    }
    @PostMapping(LOGIN)
    public ResponseEntity<TokenDto> signIn(@RequestBody AuthenticationDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
       var user = repository.findByUsername(request.getUsername())
               .orElseThrow(()->new UserNotFoundException("Пользователь не найден"));
       tokenService.revokeAllUserToken(user);
       return  ResponseEntity.ok(
               tokenService
                       .getTokenDto(
                               repository.saveAndFlush(user)
                       )
       );
    }
    @PatchMapping(EMAIL_CONFIRMATION)
    public ResponseEntity<String> confirm(@RequestParam String token) {
        var user = repository.findByEmailToken(token)
                .orElseThrow(()->new InvalidEmailTokenException("Некорректный токен подтверждения"));
        user.setEmailToken(null);
        repository.saveAndFlush(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Почта успешно подтверждена\"}");
    }
    @PutMapping(REFRESH_TOKEN)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String username;
        if(authHeader==null||!authHeader.startsWith("Bearer ")) return;
        refreshToken=authHeader.substring(7);
        username=jwtService.extractUsername(refreshToken);
        if(username!=null){
            var user = repository.findByUsername(username)
                    .orElseThrow(()->new UserNotFoundException(String.format("Пользователь %s не найден",username)));
            if(jwtService.isTokenValid(refreshToken,user)){
                var accessToken = jwtService.generateToken(user);
                tokenService.revokeAllUserToken(user);
                tokenService.saveUserToken(user,accessToken);
                var authResponse = TokenDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                ObjectMapper objectMapper = new ObjectMapper();
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(), authResponse);
            }
            else throw new InvalidJwtTokenException("Токен невалиден");
        }
    }
    @PostMapping(LOGOUT)
    public ResponseEntity<String> signOut(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String jwt=authHeader.substring(7);
        var storedToken=tokenRepository.findByToken(jwt).orElse(null);
        if(storedToken!=null){
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Вы успешно вышли из аккаунта\"}");
    }
}
