package com.chernyshev.messenger.api.exceptions;

public class MessageFriendOnlyException extends RuntimeException{
    public MessageFriendOnlyException(String message){super(message);}
}
