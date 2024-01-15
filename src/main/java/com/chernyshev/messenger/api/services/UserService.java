package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.dtos.*;
import com.chernyshev.messenger.api.exceptions.*;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private static final  String NOT_FOUND_MESSAGE ="Пользователь не найден";

    @Transactional(readOnly = true)
    public  ResponseEntity<InfoDto> getUserInfo(String username){
        var user = repository.findByUsernameAndActive(username,true)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        return ResponseEntity.ok().body(getInfoDto(user));
    }
    public ResponseEntity<String> changeUserInfo(Principal principal, String username, EditDto editDto) {
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав менять информацию о пользователе %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        Optional.ofNullable(editDto.getFirstname()).filter(s -> !s.trim().isEmpty()).ifPresent(s -> user.setFirstname(s.trim()));
        Optional.ofNullable(editDto.getLastname()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setLastname(s.trim()));
        Optional.ofNullable(editDto.getBio()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setBio(s.trim()));
        Optional.ofNullable(editDto.getStatus()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setStatus(s.trim()));
        Optional.ofNullable(editDto.getUsername()).filter(s -> s.trim().length()>=8)
                .ifPresent(
                        s-> repository.findByUsername(s.trim()).ifPresentOrElse(
                                userEntity ->{
                                    throw new UsernameAlreadyExistException(
                                            String.format(
                                                    "Пользователь %s уже существует",userEntity.getUsername()
                                            )
                                    );
                                },
                                ()-> user.setUsername(s.trim())
                        )
                );
        Optional.ofNullable(editDto.getEmail()).filter(s->!s.trim().isEmpty())
                .ifPresent(
                        s-> repository.findByEmail(s.trim())
                                .ifPresentOrElse(
                                        userEntity ->{
                                            throw new EmailAlreadyExistException(
                                                    String.format(
                                                            "Почта %s занята",userEntity.getEmail()
                                                    )
                                            );
                                        },
                                        ()->{
                                            user.setEmail(s.trim());
                                            String emailToken = UUID.randomUUID().toString();
                                            user.setEmailToken(emailToken);
                                            emailService.sendEmailConfirmationEmail(user.getEmail(), emailToken);
                                        }
                                )
                );
        repository.saveAndFlush(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Информация изменена\"}");
    }
    public ResponseEntity<String> changeUserPassword(Principal principal, String username, PasswordDto request){
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав менять пароль пользователя %s",username));
        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            throw new PasswordsNotMatchException("Неправильный пароль");
        if(!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new PasswordsNotMatchException("Пароли не совпадают");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Пароль изменен\"}");
    }
    public ResponseEntity<RecoverTokenDto> deleteUser(Principal principal,String username){
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав удалять аккаунт пользователя %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        user.setActive(false);
        return ResponseEntity.ok(tokenService.getRecoverTokenDto(repository.saveAndFlush(user)));
    }
    public ResponseEntity<TokenDto> recoverUser(RecoverTokenDto token,String username){
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
