package com.chernyshev.messenger.api.exceptions.custom;

public class InvalidEmailTokenException extends RuntimeException{
    public InvalidEmailTokenException(String message){
        super(message);
    }
}
