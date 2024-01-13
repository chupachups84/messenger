package com.chernyshev.messenger.api.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message){super(message);}
}
