package com.chernyshev.messenger.api.exceptions;

import com.chernyshev.messenger.api.exceptions.myExceptions.InvalidTokenException;
import com.chernyshev.messenger.api.exceptions.myExceptions.FriendshipException;
import com.chernyshev.messenger.api.exceptions.myExceptions.UserDeactivatedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach( (error)-> errors.add(error.getField() + " : " + error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<String> handleMailSendException(MailSendException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"Некорректный email\"}");
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("{\"message\":\""+e.getMessage()+"\"}");
    }
    @ExceptionHandler({
            UserDeactivatedException.class,
            FriendshipException.class,
            InvalidTokenException.class
    })
    public ResponseEntity<String> handleUserDeactivatedException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("{\"message\":\""+e.getMessage()+"\"}");
    }

}
