package com.chernyshev.messenger.api.controllers;

import com.chernyshev.messenger.api.dtos.InfoDto;
import com.chernyshev.messenger.api.dtos.PasswordDto;
import com.chernyshev.messenger.api.dtos.RecoverTokenDto;
import com.chernyshev.messenger.api.dtos.TokenDto;
import com.chernyshev.messenger.api.exceptions.*;
import com.chernyshev.messenger.api.services.EmailService;
import com.chernyshev.messenger.api.services.JwtService;
import com.chernyshev.messenger.api.services.TokenService;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private static final  String NOT_FOUND_MESSAGE ="Пользователь не найден";
    public static final  String GET_USER_INFO ="/api/v1/user/{username}";
    public static final  String EDIT_USER_INFO ="/api/v1/user/{username}";
    public static final  String CHANGE_USER_PASSWORD ="/api/v1/user/{username}/password-change";
    public static final  String DELETE_USER_ACCOUNT ="/api/v1/user/{username}";
    public static final  String RECOVER_USER_ACCOUNT ="/api/v1/user/{username}";

    @GetMapping(GET_USER_INFO)
    public ResponseEntity<InfoDto> info(@PathVariable String username){
        var user = repository.findByUsernameAndActive(username,true)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        return ResponseEntity.ok().body(getInfoDto(user));
    }
    @PatchMapping(EDIT_USER_INFO)
    public ResponseEntity<TokenDto> editInfo(Principal principal,@PathVariable String username,
            @RequestParam(value = "messages_friend_only",required = false) Optional<Boolean> optionalReceiveMessagesFriendOnly,
            @RequestParam(value = "hide_friends_list",required = false) Optional<Boolean> optionalFriendsListHidden,
            @RequestParam(value = "email",required = false) Optional<String >optionalNewEmail,
            @RequestParam(value = "username",required = false) Optional<String> optionalNewUsername,
            @RequestParam(value = "first_name",required = false) Optional<String> optionalFirstname,
            @RequestParam(value = "last_name",required = false) Optional<String> optionalLastname,
            @RequestParam(value = "bio",required = false) Optional<String> optionalBio,
            @RequestParam(value = "avatar_url",required = false) Optional<String> optionalAvatarUrl,
            @RequestParam(value = "status",required = false) Optional<String> optionalStatus) {

        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав менять информацию о пользователе %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));

        optionalFirstname.filter(firstname->!firstname.trim().isEmpty()).ifPresent(user::setFirstname);
        optionalLastname.filter(lastname->!lastname.trim().isEmpty()).ifPresent(user::setLastname);
        optionalBio.filter(bio->!bio.trim().isEmpty()).ifPresent(user::setBio);
        optionalAvatarUrl.filter(avatarUrl->!avatarUrl.trim().isEmpty()).ifPresent(user::setAvatarUrl);
        optionalStatus.filter(status->!status.trim().isEmpty()).ifPresent(user::setStatus);
        optionalReceiveMessagesFriendOnly.ifPresent(user::setReceiveMessagesFriendOnly);
        optionalFriendsListHidden.ifPresent(user::setFriendsListHidden);

        optionalNewUsername.filter(newUsername->newUsername.trim().length()>=8)
                .ifPresent(
                        newUsername-> repository.findByUsername(newUsername).ifPresentOrElse(
                                    userEntity ->{
                                        throw new UsernameAlreadyExistException(
                                                String.format(
                                                        "Пользователь %s уже существует",userEntity.getUsername()
                                                )
                                        );
                                    },
                                    ()-> user.setUsername(newUsername)
                            )

                );

        optionalNewEmail.filter(newEmail-> !newEmail.trim().isEmpty())
                .ifPresent(
                        email-> repository.findByEmail(email)
                                .ifPresentOrElse(
                                        userEntity ->{
                                            throw new EmailAlreadyExistException(
                                                    String.format(
                                                            "Почта %s занята",userEntity.getEmail()
                                                    )
                                            );
                                        },
                                        ()->{
                                            user.setEmail(email);
                                            String emailToken = UUID.randomUUID().toString();
                                            user.setEmailToken(emailToken);
                                            emailService.sendEmailConfirmationEmail(user.getEmail(), emailToken);
                                        }
                                )
                );

        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }

    @PatchMapping(CHANGE_USER_PASSWORD)
    public ResponseEntity<TokenDto> changePassword(Principal principal,
                                                   @PathVariable String username,
                                                   @RequestBody PasswordDto request){
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав менять пароль пользователя %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            throw new PasswordsNotMatchException("Неправильный пароль");
        if(!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new PasswordsNotMatchException("Пароли не совпадают");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }
    @DeleteMapping(DELETE_USER_ACCOUNT)
    public ResponseEntity<RecoverTokenDto> delete(Principal principal,@PathVariable String username) {
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав удалять аккаунт пользователя %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        user.setActive(false);
        return ResponseEntity.ok(tokenService.getRecoverTokenDto(repository.saveAndFlush(user)));
    }
    @PutMapping(RECOVER_USER_ACCOUNT)
    public ResponseEntity<TokenDto> recover(@RequestBody RecoverTokenDto token,@PathVariable String username) {
        String extractedUsername=jwtService.extractUsername(token.getRecoverToken());
        if(!extractedUsername.equals(username))
            throw new NoPermissionException(String.format("Нет прав восстанавливать аккаунт пользователя %s",username));
        var user = repository.findByUsernameAndActive(extractedUsername,false)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!jwtService.isTokenValid(token.getRecoverToken(),user)) throw new InvalidJwtTokenException("Токен невалиден");
        user.setActive(true);
        return ResponseEntity.ok().body(tokenService.getTokenDto(user));
    }
    private InfoDto getInfoDto(UserEntity user){
        return InfoDto.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .bio(user.getBio())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .username(user.getUsername())
                .email(user.getEmail()).build();
    }
}
