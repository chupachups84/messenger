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

    public static final  String NOT_FOUND_MESSAGE ="User not found";
    public static final  String USER_EXIST ="Username %s already exist";
    public static final  String EMAIL_EXIST ="Email %s already exist";
    public static final String INVALID_JWT_TOKEN = "Invalid Jwt token";
    public static final String INVALID_CONFIRM_TOKEN = "Invalid confirmation token";
    public static final String EMAIL_CONFIRM_SUCCESS ="Email successfully confirmed";
    public static final String NO_PERMISSION_MESSAGE ="No permission";
    public static final String PASSWORD_HAS_CHANGED="Password successfully changed";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String PASSWORDS_NOT_MATCH = "Passwords don't match";
    public static final String SELF_FRIEND_REQUEST = "Can't add yourself as a friend";
    public static final String FRIEND_REQUEST_EXIST = "Request has already sent";
    public static final String FRIEND_REQUEST_SUCCESS_SEND = "The request has been sent successfully";
    public static final String FRIEND_LIST_HIDDEN = "User %s has hidden the friends list";
    public static final String LOGOUT_SUCCESS="Logout successfully";

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

    public ResponseEntity<TokenDto> signIn(LoginDto request) throws UserNotFoundException{
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
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt=authHeader.replaceAll("^Bearer ","");
        var storedToken=tokenRepository.findByToken(jwt).orElse(null);
        assert storedToken!=null;
        storedToken.setExpired(true);
        storedToken.setRevoked(true);
        tokenRepository.saveAndFlush(storedToken);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message(LOGOUT_SUCCESS).build());
    }

    public ResponseEntity<ResponseMessageDto> emailConfirm(String token) throws InvalidEmailTokenException {
        var user = repository.findByEmailToken(token)
                .orElseThrow(()->new InvalidEmailTokenException(INVALID_CONFIRM_TOKEN));
        user.setEmailToken(null);
        repository.saveAndFlush(user);
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message(EMAIL_CONFIRM_SUCCESS).build());
    }

    public ResponseEntity<TokenDto> tokenRefresh(HttpServletRequest request)
            throws UserNotFoundException, InternalServerException,InvalidJwtTokenException{
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader==null||!authHeader.startsWith("Bearer "))
            throw  new InvalidJwtTokenException(INVALID_JWT_TOKEN);
        final String refreshToken=authHeader.replaceAll("^Bearer ","");
        final String username=jwtService.extractUsername(refreshToken);
        if(username!=null){
            var user = repository.findByUsername(username).orElseThrow();
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
                throw new InvalidJwtTokenException(INVALID_JWT_TOKEN);
        }
        else
            throw new InvalidJwtTokenException(INVALID_JWT_TOKEN);
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
            throw new NoPermissionException(NO_PERMISSION_MESSAGE);
        var user = repository.findByUsername(username).orElseThrow();
        Optional.ofNullable(
                userDto.getFirstname()).filter(s -> !s.trim().isEmpty()).ifPresent(s -> user.setFirstname(s.trim())
        );
        Optional.ofNullable(
                userDto.getLastname()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setLastname(s.trim())
        );
        Optional.ofNullable(
                userDto.getAvatarUrl()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setAvatarUrl(s.trim())
        );
        Optional.ofNullable(
                userDto.getBio()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setBio(s.trim())
        );
        Optional.ofNullable(
                userDto.getStatus()).filter(s -> !s.trim().isEmpty()).ifPresent(s->user.setStatus(s.trim())
        );
        Optional.ofNullable(
                userDto.getIsFriendsListHidden()).ifPresent(user::setFriendsListHidden
        );
        Optional.ofNullable(
                userDto.getIsReceiveMessagesFriendOnly()).ifPresent(user::setReceiptMessagesFriendOnly
        );

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

    public ResponseEntity<ResponseMessageDto> changeUserPassword(Principal principal, String username, PasswordDto request)
            throws NoPermissionException, PasswordsNotMatchException{
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(NO_PERMISSION_MESSAGE);

        var user = repository.findByUsername(principal.getName())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));

        if(!passwordEncoder.matches(request.getOldPassword(),user.getPassword()))
            throw new PasswordsNotMatchException(INVALID_PASSWORD);

        if(!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new PasswordsNotMatchException(PASSWORDS_NOT_MATCH);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.saveAndFlush(user);
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message(PASSWORD_HAS_CHANGED).build());
    }

    public ResponseEntity<RecoverTokenDto> deleteUser(Principal principal,String username)
            throws NoPermissionException,UserNotFoundException{
        if(!username.equals(principal.getName()))
            throw new NoPermissionException(NO_PERMISSION_MESSAGE);
        var user = repository.findByUsername(principal.getName()).orElseThrow();
        user.setActive(false);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(tokenService.getRecoverTokenDto(repository.saveAndFlush(user)));
    }

    public ResponseEntity<TokenDto> recoverUser(RecoverTokenDto token,String username)
            throws NoPermissionException,UserNotFoundException{
        String extractedUsername=jwtService.extractUsername(token.getRecoverToken());
        if(!extractedUsername.equals(username))
            throw new NoPermissionException(NO_PERMISSION_MESSAGE);
        var user = repository.findByUsername(extractedUsername).filter(userEntity -> !userEntity.isEnabled())
                .orElseThrow(()->new UserNotFoundException(NOT_FOUND_MESSAGE));
        if(!jwtService.isTokenValid(token.getRecoverToken(),user))
            throw new InvalidJwtTokenException(INVALID_JWT_TOKEN);
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
            throw new FriendsListHiddenException(String.format(FRIEND_LIST_HIDDEN, username));
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

    public ResponseEntity<ResponseMessageDto> addFriend(Principal principal, String username)
            throws FriendRequestException,UserNotFoundException{
        if(principal.getName().equals(username))
            throw new FriendRequestException(SELF_FRIEND_REQUEST);

        friendRepository.findByUsername1AndUsername2(principal.getName(),username)
                .ifPresentOrElse(
                       friend -> {
                           if(friend.getUser1().getUsername().equals(principal.getName())
                                   &&friend.getUser2().getUsername().equals(username)) {
                               throw new FriendRequestException(FRIEND_REQUEST_EXIST);
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
        return ResponseEntity.ok().body(ResponseMessageDto.builder().message(FRIEND_REQUEST_SUCCESS_SEND).build());
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
                .isReceiveMessagesFriendOnly(user.isReceiptMessagesFriendOnly())
                .build();
    }
}
