package com.chernyshev.messenger.api.websocket;

import org.springframework.stereotype.Component;


@Component
public class WebSocketUtils {

    public Long getId(String uri) {
        return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
    }

}
