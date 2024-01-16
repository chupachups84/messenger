package com.chernyshev.messenger.exceptions.custom;

public class MessageFriendOnlyException extends RuntimeException{
    public MessageFriendOnlyException(String message){super(message);}
}
