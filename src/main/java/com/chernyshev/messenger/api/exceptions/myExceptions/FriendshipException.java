package com.chernyshev.messenger.api.exceptions.myExceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendshipException extends RuntimeException{
    private String message;
}
