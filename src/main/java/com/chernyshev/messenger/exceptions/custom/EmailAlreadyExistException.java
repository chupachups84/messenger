package com.chernyshev.messenger.exceptions.custom;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String message){super(message);}
}
