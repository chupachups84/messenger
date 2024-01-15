package com.chernyshev.messenger.api.services;

import com.chernyshev.messenger.api.dtos.*;
import com.chernyshev.messenger.api.exceptions.*;
import com.chernyshev.messenger.store.models.FriendEntity;
import com.chernyshev.messenger.store.models.UserEntity;
import com.chernyshev.messenger.store.repositories.FriendRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final FriendRepository friendRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private static final  String NOT_FOUND_MESSAGE ="Пользователь не найден";
    public ResponseEntity<TokenDto> signUp(RegisterDto request) throws UsernameAlreadyExistException,EmailAlreadyExistException{
        if(repository.existsByUsername(request.getUsername()))
            throw new UsernameAlreadyExistException(
                    String.format("Пользователь %s уже существует",request.getUsername())
            );
        if(repository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyExistException(
                    String.format("Почта %s занята",request.getEmail())
            );

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

    public ResponseEntity<TokenDto> signIn(AuthenticationDto request) throws UserNotFoundException{
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        tokenService.revokeAllUserToken(user);
        return  ResponseEntity.ok(tokenService.getTokenDto(repository.saveAndFlush(user)));
    }

    public ResponseEntity<String> signOut(HttpServletRequest request){
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

    public ResponseEntity<String> emailConfirm(String token) throws InvalidEmailTokenException{
        var user = repository.findByEmailToken(token)
                .orElseThrow(()->new InvalidEmailTokenException("Некорректный токен подтверждения"));
        user.setEmailToken(null);
        repository.saveAndFlush(user);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Почта успешно подтверждена\"}");
    }

    public void tokenRefresh(HttpServletRequest request, HttpServletResponse response)
            throws UserNotFoundException,InternalServerException,InvalidJwtTokenException{
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
                try{
                    objectMapper.writeValue(response.getWriter(), authResponse);
                }catch (IOException ex){
                    throw new InternalServerException("IOException");
                }
            }
            else throw new InvalidJwtTokenException("Токен невалиден");
        }
    }

    @Transactional(readOnly = true)
    public  ResponseEntity<InfoDto> getUserInfo(String username) throws UserNotFoundException{
        var user = repository.findByUsernameAndActive(username,true)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        return ResponseEntity.ok().body(getInfoDto(user));
    }

    public ResponseEntity<String> changeUserInfo(Principal principal, String username, EditDto editDto)
            throws NoPermissionException ,UsernameAlreadyExistException,EmailAlreadyExistException{
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

    public ResponseEntity<String> changeUserPassword(Principal principal, String username, PasswordDto request)
            throws NoPermissionException, PasswordsNotMatchException{
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

    public ResponseEntity<RecoverTokenDto> deleteUser(Principal principal,String username)
            throws NoPermissionException,UserNotFoundException{
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав удалять аккаунт пользователя %s",username));

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        user.setActive(false);
        return ResponseEntity.ok(tokenService.getRecoverTokenDto(repository.saveAndFlush(user)));
    }

    public ResponseEntity<TokenDto> recoverUser(RecoverTokenDto token,String username)
            throws NoPermissionException,UserNotFoundException{
        String extractedUsername=jwtService.extractUsername(token.getRecoverToken());
        if(!extractedUsername.equals(username))
            throw new NoPermissionException(String.format("Нет прав восстанавливать аккаунт пользователя %s",username));
        var user = repository.findByUsernameAndActive(extractedUsername,false)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!jwtService.isTokenValid(token.getRecoverToken(),user)) throw new InvalidJwtTokenException("Токен невалиден");
        user.setActive(true);
        return ResponseEntity.ok().body(tokenService.getTokenDto(user));
    }
    @Transactional(readOnly = true)
    public ResponseEntity<List<InfoDto>> getFriendList(Principal principal, String username)
            throws UserNotFoundException,FriendsListHiddenException{
        final List<InfoDto> infoDtoList = new ArrayList<>();
        UserEntity userEntity = repository.findByUsernameAndActive(username,true)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND_MESSAGE));

        if (!principal.getName().equals(username) && userEntity.isFriendsListHidden()) {
            throw new FriendsListHiddenException(String.format("Пользователь %s скрыл список друзей", username));
        }
        friendRepository.findByUser1(userEntity).ifPresent(
                friendList -> infoDtoList.addAll(friendList.stream().map(this::getInfoDto).toList())
        );
        return ResponseEntity.ok().body(infoDtoList);
    }

    public ResponseEntity<String> addFriend(Principal principal, String username)
            throws FriendRequestException,UserNotFoundException{
        if(principal.getName().equals(username))
            throw new FriendRequestException("Нельзя добавить себя в друзья");

        var user1=repository.findByUsername(principal.getName()).orElseThrow();

        var user2= repository.findByUsernameAndActive(username,true)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));

        if(friendRepository.existsByUser1AndUser2(user1, user2))
            throw new FriendRequestException("Пользователь уже в друзьях");

        friendRepository.saveAndFlush(
                FriendEntity.builder()
                        .user1(user1)
                        .user2(user2)
                        .build()
        );
        friendRepository.saveAndFlush(
                FriendEntity.builder()
                        .user1(user2)
                        .user2(user1)
                        .build()
        );
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(String.format("{\"message\":\"Пользователь %s добавлен в друзья\"}",username));
    }

    public InfoDto getInfoDto(UserEntity user){
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
