package com.chernyshev.messenger.api.exceptions.myExceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InvalidTokenException extends RuntimeException{
    private final String message;
}
