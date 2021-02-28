package com.leartgjoni.springchatapi.service;

import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.repository.RoomMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes =  {HubService.class, RoomService.class})
class HubServiceTest {
    @Autowired
    HubService service;

    @MockBean
    RoomService roomService;

    @BeforeEach
    public void resetRooms() {
        ReflectionTestUtils.setField(service, "rooms", new ConcurrentHashMap<>());
    }

    @Test
    void getNodeId() {
        assertEquals(36, service.getNodeId().length());
    }

    @Test
    void register_noRoom() {
        User user = new User("test-id", "test-name", "test-room");
        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");

        service.register(user);

        assertEquals(1, rooms.size()); // test-room has been added
        verify(roomService, times(1)).update(user, ACTION.REGISTER);
    }

    @Test
    void register_roomExists() {
        User user = new User("test-id", "test-name", "test-room");

        Map<String, User> roomMembers = new HashMap<>();
        roomMembers.put("test-id2", new User("test-id2", "test-name2", "test-room"));
        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");
        rooms.put(user.getRoom(), roomMembers);
        rooms.put("test-room2", new HashMap<>());

        service.register(user);

        assertEquals(2, rooms.get(user.getRoom()).size());
        verify(roomService, times(1)).update(user, ACTION.REGISTER);
    }

    @Test
    void unregister_noRoom() {
        User user = new User("test-id", "test-name", "test-room");

        // doesn't throw error even if room is null
        service.unregister(user);

        verify(roomService, times(1)).update(user, ACTION.UNREGISTER);
    }

    @Test
    void unregister_roomEmptyAfterUnregister() {
        User user = new User("test-id", "test-name", "test-room");

        Map<String, User> roomMembers = new HashMap<>();
        roomMembers.put(user.getId(), user); // only one user in room - after deletion room will be empty
        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");
        rooms.put(user.getRoom(), roomMembers);
        rooms.put("test-room2", new HashMap<>());

        service.unregister(user);

        assertEquals(1, rooms.size()); // test-room has been deleted
        verify(roomService, times(1)).update(user, ACTION.UNREGISTER);
    }

    @Test
    void unregister_roomFullAfterUnregister() {
        User user = new User("test-id", "test-name", "test-room");

        Map<String, User> roomMembers = new HashMap<>();
        roomMembers.put(user.getId(), user);
        roomMembers.put("test-id2", new User("test-id2", "test-name2", "test-room"));
        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");
        rooms.put(user.getRoom(), roomMembers);
        rooms.put("test-room2", new HashMap<>());

        service.unregister(user);

        assertEquals(2, rooms.size());
        verify(roomService, times(1)).update(user, ACTION.UNREGISTER);
    }

    @Test
    void broadcast_setMessageNodeId() {
        Message message = new Message("user-id", "message", "data", "test-room");

        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");
        rooms.put(message.getRoom(), new HashMap<>());

        service.broadcast(message);

        assertEquals(service.getNodeId(), message.getNodeId()); // set node id
        verify(roomService, times(1)).publish(message);
    }

    @Test
    void broadcast() {
        WebSocketSession websocketSession = Mockito.mock(WebSocketSession.class);

        User user1 = new User("1", "user1", "test-room");
        user1.setWsSession(websocketSession);
        User user2 = new User("2", "user2", "test-room");
        user2.setWsSession(websocketSession);
        User user3 = new User("3", "user3", "test-room");
        user3.setWsSession(websocketSession);
        Message message = new Message(user1.getId(), "message", "data", user1.getRoom());
        message.setNodeId("test-node-id");

        Map<String, User> room = new HashMap<>();
        room.put(user1.getId(), user1);
        room.put(user2.getId(), user2);
        room.put(user3.getId(), user3);
        Map<String, Map<String, User>> rooms = (Map<String, Map<String, User>>) ReflectionTestUtils.getField(service, "rooms");
        rooms.put(user1.getRoom(), room);

        service.broadcast(message);

        verify(roomService, times(1)).publish(message);
        try {
            // only exe twice - skip user1 cause that's message's author
            verify(websocketSession, times(2)).sendMessage(new TextMessage(message.toJSON()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}