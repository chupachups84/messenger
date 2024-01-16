package com.chernyshev.messenger.exceptions.custom;

public class InvalidEmailTokenException extends RuntimeException{
    public InvalidEmailTokenException(String message){
        super(message);
    }
}
