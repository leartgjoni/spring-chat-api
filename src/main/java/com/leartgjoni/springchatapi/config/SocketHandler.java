package com.leartgjoni.springchatapi.config;

import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.service.HubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocketHandler extends TextWebSocketHandler {
    @Autowired
    private HubService hubService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        User user = (User) session.getAttributes().get("user");
        user.setWsSession(session);
        hubService.register(user);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        User user = (User) session.getAttributes().get("user");
        hubService.broadcast(new Message(user.getId(), "message", message.getPayload(), user.getRoom()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        User user = (User) session.getAttributes().get("user");

        if (user == null) return;

        hubService.unregister(user);
    }
}
