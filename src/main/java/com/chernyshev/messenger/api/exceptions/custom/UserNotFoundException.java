package com.chernyshev.messenger.api.exceptions.custom;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message){super(message);}
}
