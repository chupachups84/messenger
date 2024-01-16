package com.chernyshev.messenger.exceptions.custom;

public class UsernameAlreadyExistException extends RuntimeException{
    public UsernameAlreadyExistException(String message){super(message);}
}
