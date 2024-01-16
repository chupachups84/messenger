package com.chernyshev.messenger.exceptions.custom;

public class PasswordsNotMatchException extends RuntimeException{
    public PasswordsNotMatchException(String message){super(message);}
}
