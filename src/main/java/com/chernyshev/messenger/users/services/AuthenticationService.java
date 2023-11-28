package com.chernyshev.messenger.users.services;

import com.chernyshev.messenger.exception.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.security.JwtService;
import com.chernyshev.messenger.users.dtos.AuthenticationRequest;
import com.chernyshev.messenger.users.dtos.RegisterRequest;
import com.chernyshev.messenger.users.dtos.TokensResponse;
import com.chernyshev.messenger.users.models.Role;
import com.chernyshev.messenger.users.models.UserEntity;
import com.chernyshev.messenger.users.repositories.TokenRepository;
import com.chernyshev.messenger.users.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    private final TokenUtils tokenUtils;
    private final TokenRepository tokenRepository;
    public TokensResponse register(RegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) throw new IllegalStateException("Пользователь с таким username уже существует");
        if (repository.existsByEmail(request.getEmail())) throw new IllegalStateException("Пользователь с таким email уже существует");
        String emailConfirmationToken = UUID.randomUUID().toString();
        var user = UserEntity.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .privateProfile(false)
                .emailConfirmationToken(emailConfirmationToken)
                .build();

        emailService.sendEmailConfirmationEmail(user.getEmail(), emailConfirmationToken);
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenUtils.saveUserToken(savedUser, jwtToken);
        return TokensResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokensResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername()).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenUtils.revokeAllUserToken(user);
        tokenUtils.saveUserToken(user,jwtToken);
        return TokensResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String username;
        if(authHeader==null||!authHeader.startsWith("Bearer ")) return;
        refreshToken=authHeader.substring(7);
        username=jwtService.extractUsername(refreshToken);
        if(username!=null){
            var user = this.repository.findByUsername(username).orElseThrow();
            if(jwtService.isTokenValid(refreshToken,user)){
                var accessToken = jwtService.generateToken(user);
                tokenUtils.revokeAllUserToken(user);
                tokenUtils.saveUserToken(user,accessToken);
                var authResponse = TokensResponse.builder()
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
        return ResponseEntity.ok().body("Почта успешно подтверждена.");
    }
    public ResponseEntity<String> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
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
        return ResponseEntity.ok().body("Вы успешно вышли из аккаунта");
    }
}
