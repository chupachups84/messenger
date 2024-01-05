package com.chernyshev.messenger.api.exceptions;

import com.chernyshev.messenger.api.dtos.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorDto.builder()
                        .error("Not Found")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleBadRequestException(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorDto.builder()
                        .error("Bad Request")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDto> handleForbiddenException(ForbiddenException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorDto.builder()
                        .error("Forbidden")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorDto> handleForbiddenException(UnauthorizedException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorDto.builder()
                        .error("Unauthorized")
                        .errorDescription(ex.getMessage())
                        .build());

    }
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorDto> handleMailException(MailException ex){
        return ResponseEntity.status(550).body(
                ErrorDto.builder()
                        .error("Unroutable Sender Address")
                        .errorDescription("Почта не существует")
                        .build());
    }
}
