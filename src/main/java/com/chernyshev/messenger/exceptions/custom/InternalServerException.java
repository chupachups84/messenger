package com.chernyshev.messenger.exceptions.custom;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String s) {
        super(s);
    }
}
