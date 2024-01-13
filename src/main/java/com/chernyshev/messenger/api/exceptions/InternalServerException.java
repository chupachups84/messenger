package com.chernyshev.messenger.api.exceptions;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String s) {
        super(s);
    }
}
