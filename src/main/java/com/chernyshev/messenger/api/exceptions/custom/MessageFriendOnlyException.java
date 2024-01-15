package com.chernyshev.messenger.api.exceptions.custom;

public class MessageFriendOnlyException extends RuntimeException{
    public MessageFriendOnlyException(String message){super(message);}
}
