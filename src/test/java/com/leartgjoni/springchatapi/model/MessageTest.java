package com.leartgjoni.springchatapi.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void toJSON() {
        Message message = new Message("user-id", "msg", "data", "room-id");
        assertEquals(new Gson().toJson(message), message.toJSON());
    }
}