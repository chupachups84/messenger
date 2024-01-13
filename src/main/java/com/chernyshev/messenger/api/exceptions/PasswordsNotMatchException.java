package com.chernyshev.messenger.api.exceptions;

public class PasswordsNotMatchException extends RuntimeException{
    public PasswordsNotMatchException(String message){super(message);}
}
