package com.leartgjoni.springchatapi.config;

import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.service.HubService;
import com.leartgjoni.springchatapi.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes =  {SocketHandler.class, HubService.class})
class SocketHandlerTest {
    @Autowired
    SocketHandler handler;

    @MockBean
    HubService hubService;

    @Test
    void afterConnectionEstablished() {
        User user = new User("test-id", "test-name", "test-room");
        WebSocketSession webSocketSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        when(webSocketSession.getAttributes()).thenReturn(attributes);

        handler.afterConnectionEstablished(webSocketSession);

        assertEquals(webSocketSession, user.getWsSession());
        verify(hubService, times(1)).register(user);
    }

    @Test
    void handleTextMessage() {
        User user = new User("test-id", "test-name", "test-room");
        WebSocketSession webSocketSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        when(webSocketSession.getAttributes()).thenReturn(attributes);

        String msgPayload = "msg payload data";
        TextMessage textMessage = new TextMessage(msgPayload);

        handler.handleTextMessage(webSocketSession, textMessage);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(hubService, times(1)).broadcast(captor.capture());

        Message message = captor.getValue();
        assertEquals(message.getUserId(), user.getId());
        assertEquals(message.getType(), "message");
        assertEquals(message.getData(), msgPayload);
        assertEquals(message.getRoom(), user.getRoom());
    }

    @Test
    void afterConnectionClosed_noUser() {
        User user = new User("test-id", "test-name", "test-room");
        WebSocketSession webSocketSession = mock(WebSocketSession.class);

        handler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL);

        verify(hubService, never()).unregister(user);
    }

    @Test
    void afterConnectionClosed_withUser() {
        User user = new User("test-id", "test-name", "test-room");
        WebSocketSession webSocketSession = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", user);
        when(webSocketSession.getAttributes()).thenReturn(attributes);

        handler.afterConnectionClosed(webSocketSession, CloseStatus.NORMAL);

        verify(hubService, times(1)).unregister(user);
    }
}