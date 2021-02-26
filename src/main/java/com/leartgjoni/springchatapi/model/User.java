package com.leartgjoni.springchatapi.model;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class User {
    private String id;
    private String name;
    private String room;
    private WebSocketSession wsSession;

    public User(String id, String name, String room) {
        this.id = id;
        this.name = name;
        this.room = room;
    }
}
