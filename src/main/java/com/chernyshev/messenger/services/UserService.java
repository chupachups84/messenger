package com.chernyshev.messenger.services;

import com.chernyshev.messenger.dtos.*;
import com.chernyshev.messenger.exceptions.custom.*;
import com.chernyshev.messenger.models.FriendEntity;
import com.chernyshev.messenger.models.UserEntity;
import com.chernyshev.messenger.models.enums.StatusType;
import com.chernyshev.messenger.repositories.FriendRepository;
import com.chernyshev.messenger.repositories.TokenRepository;
import com.chernyshev.messenger.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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

import java.security.Principal;
import java.util.*;

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
    private static final  String USER_EXIST ="Пользователь %s уже существует";
    private static final  String EMAIL_EXIST ="Почта %s занята";
    private static final String INVALID_TOKEN = "Токен невалиден";
    public ResponseEntity<TokenDto> signUp(RegisterDto request) throws UsernameAlreadyExistException, EmailAlreadyExistException {
        repository.findByUsername(request.getUsername()).ifPresent(
                userEntity->{
                    throw new UsernameAlreadyExistException(String.format(USER_EXIST,request.getUsername()));
                }
            );
        repository.findByEmail(request.getEmail()).ifPresent(
                userEntity->{
                    throw new EmailAlreadyExistException(String.format(EMAIL_EXIST,request.getEmail()));
                }
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

    public ResponseEntity<ResponseMessageDto> signOut(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        String jwt=authHeader.substring(7);
        var storedToken=tokenRepository.findByToken(jwt).orElse(null);
        assert storedToken!=null;
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.saveAndFlush(storedToken);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message("Вы успешно вышли из аккаунта").build());
    }

    public ResponseEntity<ResponseMessageDto> emailConfirm(String token) throws InvalidEmailTokenException {
        var user = repository.findByEmailToken(token)
                .orElseThrow(()->new InvalidEmailTokenException("Некорректный токен подтверждения"));
        user.setEmailToken(null);
        repository.saveAndFlush(user);
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message("Почта успешно подтверждена").build());
    }

    public ResponseEntity<TokenDto> tokenRefresh(HttpServletRequest request)
            throws UserNotFoundException, InternalServerException,InvalidJwtTokenException{
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken;
        String username;
        if(authHeader==null||!authHeader.startsWith("Bearer "))
            throw  new InvalidJwtTokenException(INVALID_TOKEN);
        refreshToken=authHeader.substring(7);
        username=jwtService.extractUsername(refreshToken);
        if(username!=null){
            var user = repository.findByUsername(username)
                    .orElseThrow(()->new UserNotFoundException(String.format("Пользователь %s не найден",username)));
            if(jwtService.isTokenValid(refreshToken,user)){
                var accessToken = jwtService.generateToken(user);
                tokenService.revokeAllUserToken(user);
                tokenService.saveUserToken(user,accessToken);
                return ResponseEntity.ok().body(
                        TokenDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build()
                );
            }
            else
                throw new InvalidJwtTokenException(INVALID_TOKEN);
        }
        else
            throw new InvalidJwtTokenException(INVALID_TOKEN);
    }

    @Transactional(readOnly = true)
    public  ResponseEntity<UserDto> getUserInfo(String username) throws UserNotFoundException{
        var user = repository.findByUsername(username).filter(UserEntity::isEnabled)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        return ResponseEntity.ok().body(convertToUserDto(user));
    }

    public ResponseEntity<TokenDto> changeUserInfo(Principal principal, String username, UserDto userDto)
            throws NoPermissionException ,UsernameAlreadyExistException,EmailAlreadyExistException{
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(String.format("Нет прав менять информацию о пользователе %s",username));
        var user = repository.findByUsername(username)
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        Optional.ofNullable(userDto.getFirstname()).filter(s -> !s.trim().isEmpty()).ifPresent(s -> user.setFirstname(s.trim()));
        Optional.ofNullable(userDto.getLastname()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setLastname(s.trim()));
        Optional.ofNullable(userDto.getBio()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setBio(s.trim()));
        Optional.ofNullable(userDto.getStatus()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setStatus(s.trim()));
        Optional.ofNullable(userDto.getIsFriendsListHidden()).ifPresent(user::setFriendsListHidden);
        Optional.ofNullable(userDto.getIsReceiveMessagesFriendOnly()).ifPresent(user::setReceiveMessagesFriendOnly);

        Optional.ofNullable(userDto.getEmail()).filter(s->!s.trim().isEmpty())
                .ifPresent(
                        s-> repository.findByEmail(s.trim())
                                .ifPresentOrElse(
                                        userEntity ->{
                                            throw new EmailAlreadyExistException(
                                                    String.format(EMAIL_EXIST,userEntity.getEmail())
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
        Optional.ofNullable(userDto.getUsername()).filter(s -> s.trim().length()>=8)
                .ifPresent(
                        s-> {
                            final String trimUsername=s.trim();
                            repository.findByUsername(trimUsername).ifPresentOrElse(
                                    userEntity ->{
                                        throw new UsernameAlreadyExistException(
                                                String.format(USER_EXIST,userEntity.getUsername())
                                        );
                                    },
                                    ()-> user.setUsername(trimUsername)
                            );
                        }
                );
        return ResponseEntity.ok().body(tokenService.getTokenDto(repository.saveAndFlush(user)));

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
        repository.saveAndFlush(user);
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
        var user = repository.findByUsername(extractedUsername).filter(userEntity -> !userEntity.isEnabled())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!jwtService.isTokenValid(token.getRecoverToken(),user))
            throw new InvalidJwtTokenException(INVALID_TOKEN);
        user.setActive(true);
        return ResponseEntity.ok().body(tokenService.getTokenDto(user));
    }
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserDto>> getFriendList(Principal principal, String username)
            throws UserNotFoundException, FriendsListHiddenException {
        final List<UserDto> userDtoList = new ArrayList<>();
        var user = repository.findByUsername(username).filter(UserEntity::isEnabled)
                .orElseThrow(() -> new UserNotFoundException(NOT_FOUND_MESSAGE));

        if (!principal.getName().equals(username) && user.isFriendsListHidden()) {
            throw new FriendsListHiddenException(String.format("Пользователь %s скрыл список друзей", username));
        }
        friendRepository.findAllByUsername(username).ifPresent(
                friendList -> userDtoList.addAll(
                        friendList.stream().map(
                                friend -> {
                                    if(friend.getUser1().getUsername().equals(username))
                                        return convertToUserDto(friend.getUser2());
                                    else
                                        return convertToUserDto(friend.getUser1());
                                }
                        ).sorted(Comparator.comparing(UserDto::getLastname)).toList()
                )
        );
        return ResponseEntity.ok().body(userDtoList);
    }

    public ResponseEntity<String> addFriend(Principal principal, String username)
            throws FriendRequestException,UserNotFoundException{
        if(principal.getName().equals(username))
            throw new FriendRequestException("Нельзя добавить себя в друзья");

        friendRepository.findByUsername1AndUsername2(principal.getName(),username)
                .ifPresentOrElse(
                       friend -> {
                           if(friend.getUser1().getUsername().equals(principal.getName())
                                   &&friend.getUser2().getUsername().equals(username)) {
                               throw new FriendRequestException("Заявка уже отправлена");
                           }
                           else{
                               friend.setStatusType(StatusType.APPROVED);
                               friendRepository.saveAndFlush(friend);
                           }
                       },
                       ()->{
                           var user1 = repository.findByUsername(principal.getName()).orElseThrow();
                           var user2 = repository.findByUsername(username).filter(UserEntity::isEnabled)
                                   .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
                           friendRepository.saveAndFlush(
                                   FriendEntity.builder()
                                           .user1(user1)
                                           .user2(user2)
                                           .statusType(StatusType.REQUESTED)
                                           .build()
                           );
                       }
                );
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Запрос отправлен\"}");
    }




    public UserDto convertToUserDto(UserEntity user){
        return UserDto.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .bio(user.getBio())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .username(user.getUsername())
                .email(user.getEmail())
                .isFriendsListHidden(user.isFriendsListHidden())
                .isReceiveMessagesFriendOnly(user.isReceiveMessagesFriendOnly())
                .build();
    }
}
