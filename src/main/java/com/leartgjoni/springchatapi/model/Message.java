package com.leartgjoni.springchatapi.model;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Message implements Serializable {
    private String userId;
    private String type;
    private String data;
    private String room;
    private String nodeId;

    public Message(String userId, String type, String data, String room) {
        this.userId = userId;
        this.type = type;
        this.data = data;
        this.room = room;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
