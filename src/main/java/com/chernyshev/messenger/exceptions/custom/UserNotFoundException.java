package com.chernyshev.messenger.exceptions.custom;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message){super(message);}
}
