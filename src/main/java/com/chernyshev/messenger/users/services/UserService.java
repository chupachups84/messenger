package com.chernyshev.messenger.users.services;

import com.chernyshev.messenger.exception.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.security.JwtService;
import com.chernyshev.messenger.users.dtos.*;
import com.chernyshev.messenger.users.models.UserEntity;
import com.chernyshev.messenger.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final TokenUtils tokenUtils;

    public InfoResponse info(String username) throws UserDeactivatedException {
        var user = repository.findByUsername(username).orElseThrow();
        if (!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        return InfoResponse.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .bio(user.getBio())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .username(user.getUsername())
                .email(user.getEmail()).build();

    }
    public void edit(InfoRequest infoRequest, String username) throws UserDeactivatedException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        if (infoRequest.getFirstname() != null) user.setFirstname(infoRequest.getFirstname());
        if (infoRequest.getLastname() != null) user.setLastname(infoRequest.getLastname());
        if (infoRequest.getBio() != null) user.setBio(infoRequest.getBio());
        if (infoRequest.getAvatarUrl() != null) user.setAvatarUrl(infoRequest.getAvatarUrl());
        if (infoRequest.getStatus() != null) user.setStatus(infoRequest.getStatus());
        repository.save(user);
    }
    public TokensResponse changeUsername(UsernameRequest request, String username) throws UserDeactivatedException, IllegalStateException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException( "Пользователь неактивен");
        if (repository.existsByUsername(request.getUsername())) throw new IllegalStateException("Пользователь с таким username уже существует");
        user.setUsername(request.getUsername());
        return getTokensResponse(user);
    }

    public TokensResponse changeEmail(EmailRequest request, String username) throws UserDeactivatedException, IllegalStateException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException( "Пользователь неактивен");
        if (repository.existsByEmail(request.getEmail())) throw new IllegalStateException("Пользователь с таким email уже существует");
        user.setEmail(request.getEmail());
        String emailConfirmationToken = UUID.randomUUID().toString();
        user.setEmailConfirmationToken(emailConfirmationToken);
        emailService.sendEmailConfirmationEmail(user.getEmail(), emailConfirmationToken);
        return getTokensResponse(user);
    }
    public TokensResponse changePassword(PasswordRequest request, String username) throws IllegalStateException, UserDeactivatedException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");

        if(request.getOldPassword()!=null&&request.getNewPassword()!=null
                && passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        else throw new IllegalStateException("Неправильный пароль");
        return getTokensResponse(user);
    }
    public TokensResponse delete(String username) throws UserDeactivatedException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        user.setActive(false);
        repository.save(user);
        return getTokensResponse(user);
    }
    public TokensResponse recover(String username){
        var user = repository.findDeactivatedByUsername(username).orElseThrow();
        user.setActive(true);
        repository.save(user);
        return getTokensResponse(user);
    }
    public void changeProfilePrivacy(String username) throws UserDeactivatedException {
        var user = repository.findByUsername(username).orElseThrow();
        if(!user.isActive()) throw new UserDeactivatedException("Пользователь неактивен");
        user.setPrivateProfile(!user.isPrivateProfile());
        repository.save(user);
    }
    private TokensResponse getTokensResponse(UserEntity user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        repository.save(user);
        tokenUtils.revokeAllUserToken(user);
        tokenUtils.saveUserToken(user,jwtToken);
        return TokensResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

}
