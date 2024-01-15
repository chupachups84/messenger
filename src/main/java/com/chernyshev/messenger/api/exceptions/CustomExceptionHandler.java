package com.chernyshev.messenger.api.exceptions;

import com.chernyshev.messenger.api.dtos.ErrorDto;
import com.chernyshev.messenger.api.exceptions.custom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(UserNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorDto.builder()
                        .error("Not Found")
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
                        .error("Bad Request")
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
                        .error("Forbidden")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler({
            InvalidEmailTokenException.class,
            InvalidUsernameOrPasswordException.class,
            InvalidJwtTokenException.class
    })
    public ResponseEntity<ErrorDto> handleUnauthorizedException(Exception ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorDto.builder()
                        .error("Unauthorized")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorDto> handleMailException(){
        return ResponseEntity.status(550).body(
                ErrorDto.builder()
                        .error("Unroutable Sender Address")
                        .errorDescription("Почта не существует")
                        .build());
    }
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorDto> handleInternalServerException(InternalServerException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorDto.builder()
                        .error("Internal Server Error")
                        .errorDescription(ex.getMessage())
                        .build());
    }
}
