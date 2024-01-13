package com.chernyshev.messenger.api.exceptions;

public class UsernameAlreadyExistException extends RuntimeException{
    public UsernameAlreadyExistException(String message){super(message);}
}
