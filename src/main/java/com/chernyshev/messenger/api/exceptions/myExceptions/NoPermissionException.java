package com.chernyshev.messenger.api.exceptions.myExceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoPermissionException extends RuntimeException {
    public final String message;
}
