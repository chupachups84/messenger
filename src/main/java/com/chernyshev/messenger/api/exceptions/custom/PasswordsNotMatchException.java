package com.chernyshev.messenger.api.exceptions.custom;

public class PasswordsNotMatchException extends RuntimeException{
    public PasswordsNotMatchException(String message){super(message);}
}
