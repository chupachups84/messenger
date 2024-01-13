package com.chernyshev.messenger.api.exceptions;

public class InvalidUsernameOrPasswordException extends RuntimeException{
    public InvalidUsernameOrPasswordException(String message){
        super(message);
    }
}
