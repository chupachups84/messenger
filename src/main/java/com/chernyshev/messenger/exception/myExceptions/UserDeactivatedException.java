package com.chernyshev.messenger.exception.myExceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeactivatedException extends RuntimeException{
    private final String message;
}
