package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
import com.chernyshev.messenger.api.dtos.PasswordDto;
import com.chernyshev.messenger.api.dtos.TokenDto;
import com.chernyshev.messenger.api.email.EmailService;
import com.chernyshev.messenger.api.exceptions.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.api.services.TokenService;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    public final String EDIT ="/edit";
    public final String CHANGE_USERNAME = "/change-username";
    public final String CHANGE_EMAIL = "/change-email";
    public final String CHANGE_PASSWORD = "/change-password";
    public final String DELETE = "/delete";
    public final String RECOVER = "/recover";
    public final String CHANGE_PROFILE_PRIVACY = "/change-profile-privacy";

    @GetMapping()
    public ResponseEntity<InfoDto> info(Principal principal){

        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        return ResponseEntity.ok().body(
                InfoDto.builder()
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .bio(user.getBio())
                        .status(user.getStatus())
                        .avatarUrl(user.getAvatarUrl())
                        .username(user.getUsername())
                        .email(user.getEmail()).build());
    }
    @PatchMapping(EDIT)
    public ResponseEntity<String> edit(
            @RequestParam(value = "first_name",required = false) Optional<String> optionalFirstname,
            @RequestParam(value = "last_name",required = false) Optional<String> optionalLastname,
            @RequestParam(value = "bio",required = false) Optional<String> optionalBio,
            @RequestParam(value = "avatar_url",required = false) Optional<String> optionalAvatarUrl,
            @RequestParam(value = "status",required = false) Optional<String> optionalStatus,
            Principal principal) {
        String username = principal.getName();
        var user = repository.findByUsername(username).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        optionalFirstname.filter(firstname->!firstname.trim().isEmpty()).ifPresent(user::setFirstname);
        optionalLastname.filter(lastname->!lastname.trim().isEmpty()).ifPresent(user::setLastname);
        optionalBio.filter(bio->!bio.trim().isEmpty()).ifPresent(user::setBio);
        optionalAvatarUrl.filter(avatarUrl->!avatarUrl.trim().isEmpty()).ifPresent(user::setAvatarUrl);
        optionalStatus.filter(status->!status.trim().isEmpty()).ifPresent(user::setStatus);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Информация успешно изменена\"}");
    }
    @PostMapping(CHANGE_USERNAME)
    public ResponseEntity<TokenDto> changeUsername(@RequestParam(value = "username") String newUsername, Principal principal) {
        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        repository.findByUsername(newUsername)
                .ifPresent((anotherUser)->{
                    throw new IllegalStateException(String.format("Пользователь \"%s\" уже существует",anotherUser.getUsername()));
                });
        user.setUsername(newUsername);
        return ResponseEntity.ok(tokenService.getTokenDto(user));
    }
    @PostMapping(CHANGE_EMAIL)
    public ResponseEntity<TokenDto> changeEmail(@RequestParam(value = "email") String newEmail, Principal principal) {
        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        repository.findByUsername(newEmail)
                .ifPresent((anotherUser)->{
                    throw new IllegalStateException(String.format("Почта \"%s\" занята",anotherUser.getEmail()));
                });
        user.setEmail(newEmail);
        String emailConfirmationToken = UUID.randomUUID().toString();
        user.setEmailConfirmationToken(emailConfirmationToken);
        emailService.sendEmailConfirmationEmail(user.getEmail(), emailConfirmationToken);
        return ResponseEntity.ok(tokenService.getTokenDto(user));
    }
    @PostMapping(CHANGE_PASSWORD)
    public ResponseEntity<TokenDto> changePassword(@RequestBody @Valid PasswordDto request, Principal principal){
        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));

        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            throw new IllegalStateException("Неправильный пароль");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        return ResponseEntity.ok(tokenService.getTokenDto(user));
    }
    @DeleteMapping(DELETE)
    public ResponseEntity<TokenDto> delete(Principal principal) {
        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        user.setActive(false);
        return ResponseEntity.ok(tokenService.getTokenDto(user));
    }
    @PostMapping(RECOVER)
    public ResponseEntity<TokenDto> recover(Principal principal) {
        var user = repository.findDeactivatedByUsername(principal.getName()).orElseThrow();
        user.setActive(true);
        return ResponseEntity.ok(tokenService.getTokenDto(user));
    }
    @PatchMapping(CHANGE_PROFILE_PRIVACY )
    public ResponseEntity<String> setPrivateProfile(Principal principal){
        var user = repository.findByUsername(principal.getName()).filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        user.setPrivateProfile(!user.isPrivateProfile());
        return ResponseEntity.ok().build();
    }


}
