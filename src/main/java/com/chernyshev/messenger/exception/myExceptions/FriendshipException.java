package com.chernyshev.messenger.exception.myExceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendshipException extends RuntimeException{
    private String message;
}
