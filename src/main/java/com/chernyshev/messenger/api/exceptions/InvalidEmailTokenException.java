package com.chernyshev.messenger.api.exceptions;

public class InvalidEmailTokenException extends RuntimeException{
    public InvalidEmailTokenException(String message){
        super(message);
    }
}
