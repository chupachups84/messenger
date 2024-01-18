package com.chernyshev.messenger.exceptions;

import com.chernyshev.messenger.dtos.ErrorDto;
import com.chernyshev.messenger.exceptions.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {
    public static final String NOT_FOUND_ERROR="Not Found";
    public static final String BAD_REQUEST_ERROR="Bad Request";
    public static final String FORBIDDEN_ERROR="Forbidden";
    public static final String UNAUTHORIZED_ERROR="Unauthorized";
    public static final String UNROUTABLE_SENDER_ADDRESS_ERROR="Unroutable Sender Address";
    public static final String INTERNAL_SERVER_ERROR="Internal Server Error";

    @ExceptionHandler({
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorDto> handleNotFoundException(Exception ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorDto.builder()
                        .error(NOT_FOUND_ERROR)
                        .errorDescription(ex.getMessage())
                        .build());

    }

    @ExceptionHandler({
            EmailAlreadyExistException.class,
            FriendRequestException.class,
            PasswordsNotMatchException.class,
            UsernameAlreadyExistException.class
    })
    public ResponseEntity<ErrorDto> handleBadRequestException(Exception ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorDto.builder()
                        .error(BAD_REQUEST_ERROR)
                        .errorDescription(ex.getMessage())
                        .build());

    }

    @ExceptionHandler({
            FriendsListHiddenException.class,
            MessageFriendOnlyException.class,
            NoPermissionException.class
    })
    public ResponseEntity<ErrorDto> handleForbiddenException(Exception ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorDto.builder()
                        .error(FORBIDDEN_ERROR)
                        .errorDescription(ex.getMessage())
                        .build());

    }

    @ExceptionHandler({
            InvalidEmailTokenException.class,
            InvalidUsernameOrPasswordException.class,
            InvalidJwtTokenException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorDto> handleUnauthorizedException(Exception ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorDto.builder()
                        .error(UNAUTHORIZED_ERROR)
                        .errorDescription(ex.getMessage())
                        .build());

    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorDto> handleMailException(){
        return ResponseEntity.status(550).body(
                ErrorDto.builder()
                        .error(UNROUTABLE_SENDER_ADDRESS_ERROR)
                        .errorDescription("Почта не существует")
                        .build());
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDto> handleInternalServerException(InternalServerException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorDto.builder()
                        .error(INTERNAL_SERVER_ERROR)
                        .errorDescription(ex.getMessage())
                        .build());
    }
}
