package com.chernyshev.messenger.api.exceptions.custom;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String message){super(message);}
}
