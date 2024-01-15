package com.chernyshev.messenger.api.exceptions.custom;

public class UsernameAlreadyExistException extends RuntimeException{
    public UsernameAlreadyExistException(String message){super(message);}
}
