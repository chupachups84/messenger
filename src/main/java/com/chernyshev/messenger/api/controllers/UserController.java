package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
import com.chernyshev.messenger.api.dtos.PasswordDto;
import com.chernyshev.messenger.api.dtos.TokenDto;
import com.chernyshev.messenger.api.exceptions.myExceptions.NoPermissionException;
import com.chernyshev.messenger.api.services.EmailService;
import com.chernyshev.messenger.api.exceptions.myExceptions.UserDeactivatedException;
import com.chernyshev.messenger.api.services.TokenService;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;


    @GetMapping("/{id}")
    public ResponseEntity<InfoDto> info(Principal principal,@PathVariable Long id){
        var user = repository.findById(id)
                .filter(UserEntity::isActive)
                .orElseThrow(()->new UserDeactivatedException("Пользователь неактивен"));
        return ResponseEntity.ok().body(
                InfoDto.builder()
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .bio(user.getBio())
                        .status(user.getStatus())
                        .avatarUrl(user.getAvatarUrl())
                        .username(user.getUsername())
                        .email(user.getEmail()).build()
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<TokenDto> edit(Principal principal,@PathVariable Long id,
            @RequestParam(value = "profile_privacy",required = false) Optional<Boolean> optionalProfilePrivacy,
            @RequestParam(value = "email",required = false) Optional<String >optionalNewEmail,
            @RequestParam(value = "username",required = false) Optional<String> optionalNewUsername,
            @RequestParam(value = "first_name",required = false) Optional<String> optionalFirstname,
            @RequestParam(value = "last_name",required = false) Optional<String> optionalLastname,
            @RequestParam(value = "bio",required = false) Optional<String> optionalBio,
            @RequestParam(value = "avatar_url",required = false) Optional<String> optionalAvatarUrl,
            @RequestParam(value = "status",required = false) Optional<String> optionalStatus) {

        var user = repository.findByUsername(principal.getName())
                .filter(UserEntity::isActive)
                .filter(u -> u.getId().equals(id) )
                .orElseThrow(()-> new NoPermissionException("Пользователь неактивен или нет прав доступа"));

        optionalFirstname.filter(firstname->!firstname.trim().isEmpty()).ifPresent(user::setFirstname);
        optionalLastname.filter(lastname->!lastname.trim().isEmpty()).ifPresent(user::setLastname);
        optionalBio.filter(bio->!bio.trim().isEmpty()).ifPresent(user::setBio);
        optionalAvatarUrl.filter(avatarUrl->!avatarUrl.trim().isEmpty()).ifPresent(user::setAvatarUrl);
        optionalStatus.filter(status->!status.trim().isEmpty()).ifPresent(user::setStatus);
        optionalProfilePrivacy.ifPresent(user::setPrivateProfile);

        optionalNewUsername.filter(newUsername->newUsername.trim().length()>=8)
                .ifPresent(
                        (username)->{
                            repository.findByUsername(username).ifPresentOrElse(
                                    (userEntity) ->{
                                        throw new IllegalStateException(String.format("Пользователь \"%s\" уже существует",userEntity.getUsername()));
                                    },
                                    ()->{
                                        user.setUsername(username);
                                    }
                            );
                        }
                );

        optionalNewEmail.filter(newUsername-> !newUsername.trim().isEmpty())
                .ifPresent(
                        (email)->{
                            repository.findByEmail(email).ifPresentOrElse(
                                    (userEntity) ->{
                                        throw new IllegalStateException(String.format("Почта \"%s\" занята",userEntity.getEmail()));
                                    },
                                    ()->{
                                        user.setEmail(email);
                                        String emailConfirmationToken = UUID.randomUUID().toString();
                                        user.setEmailConfirmationToken(emailConfirmationToken);
                                        emailService.sendEmailConfirmationEmail(user.getEmail(), emailConfirmationToken);
                                    }
                            );
                        }
                );

        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TokenDto> changePassword(Principal principal,@PathVariable Long id ,@RequestBody @Valid PasswordDto request){
        var user = repository.findByUsername(principal.getName())
                .filter(UserEntity::isActive)
                .filter(u -> u.getId().equals(id) )
                .orElseThrow(()-> new NoPermissionException("Пользователь неактивен или нет прав доступа"));

        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            throw new IllegalStateException("Неправильный пароль");
        if(!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new IllegalStateException("Пароли не совпадают");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<TokenDto> delete(Principal principal,@PathVariable Long id) {
        var user = repository.findByUsername(principal.getName())
                .filter(UserEntity::isActive)
                .filter(u -> u.getId().equals(id) )
                .orElseThrow(()-> new NoPermissionException("Пользователь неактивен или нет прав доступа"));
        user.setActive(false);
        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<TokenDto> recover(Principal principal,@PathVariable Long id) {
        var user = repository.findDeactivatedByUsername(principal.getName())
                .filter(u -> u.getId().equals(id) )
                .orElseThrow(()-> new NoPermissionException("Нет прав доступа"));
        user.setActive(true);
        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }


}
