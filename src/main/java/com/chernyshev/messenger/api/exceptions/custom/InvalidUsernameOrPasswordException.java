package com.chernyshev.messenger.api.exceptions.custom;

public class InvalidUsernameOrPasswordException extends RuntimeException{
    public InvalidUsernameOrPasswordException(String message){
        super(message);
    }
}
