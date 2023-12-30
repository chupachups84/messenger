package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.AuthenticationDto;
import com.chernyshev.messenger.dtos.TokenDto;
import com.chernyshev.messenger.exception.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.models.Role;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.repositories.TokenRepository;
import com.chernyshev.messenger.repositories.UserRepository;
import com.chernyshev.messenger.security.JwtService;
import com.chernyshev.messenger.dtos.RegisterDto;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    public TokenDto register(RegisterDto request) {
        repository.findByUsername(request.getUsername())
                    .ifPresent(user->{
                        throw new IllegalStateException(String.format("Пользователь \"%s\" уже существует",request.getUsername()));
                    });
        repository.findByEmail(request.getEmail())
                    .ifPresent(user->{
                        throw new IllegalStateException(String.format("Пользователь с почтой \"%s\" уже существует",request.getEmail()));
                    });

        String emailConfirmationToken = UUID.randomUUID().toString();

        UserEntity user = UserEntity.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .emailConfirmationToken(emailConfirmationToken)
                .build();

        emailService.sendEmailConfirmationEmail(user.getEmail(), emailConfirmationToken);
        var savedUser = repository.save(user);
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenService.saveUserToken(savedUser, accessToken);
        return tokenService.makeTokenDto(accessToken,refreshToken);
    }

    public TokenDto login(AuthenticationDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername()).orElseThrow();
        if(!user.isActive())
            throw new UserDeactivatedException("Пользователь неактивен");
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenService.revokeAllUserToken(user);
        tokenService.saveUserToken(user,accessToken);
        return tokenService.makeTokenDto(accessToken,refreshToken);
    }
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String jwt;
        jwt=authHeader.substring(7);
        String username=jwtService.extractUsername(jwt);
        var user=repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        var storedToken=tokenRepository.findByToken(jwt).orElse(null);
        if(storedToken!=null){
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Вы успешно вышли из аккаунта\"}");
    }
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String username;
        if(authHeader==null||!authHeader.startsWith("Bearer ")) return;
        refreshToken=authHeader.substring(7);
        username=jwtService.extractUsername(refreshToken);
        if(username!=null){
            var user = repository.findByUsername(username).orElseThrow();
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
        }
    }
    public ResponseEntity<String> confirmation(String emailConfirmationToken) {
        var user = repository.findByEmailConfirmationToken(emailConfirmationToken).orElseThrow(()->new InvalidTokenException("Некорректный токен подтверждения"));
        user.setEmailConfirmationToken(null);
        repository.save(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Почта успешно подтверждена\"}");
    }

}
