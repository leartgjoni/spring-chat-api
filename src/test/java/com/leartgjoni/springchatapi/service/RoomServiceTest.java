package com.leartgjoni.springchatapi.service;

import com.google.gson.Gson;
import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.RoomMember;
import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.repository.RoomMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes =  {RoomService.class, RoomMemberRepository.class, HubService.class})
class RoomServiceTest {
    @Autowired
    RoomService roomService;

    @MockBean
    RoomMemberRepository roomMemberRepository;

    @MockBean
    HubService hubService;

    @Test
    void update_register() {
        User user = new User("test-id", "test-name", "test-room");

        Map<String, RoomMember> userLists = new HashMap<>();
        userLists.put(user.getId(), new RoomMember(user.getName()));
        userLists.put("test-id2", new RoomMember("test-name2"));
        when(roomMemberRepository.getAll(user.getRoom())).thenReturn(userLists);

        roomService.update(user, ACTION.REGISTER);

        verify(roomMemberRepository, times(1)).create(user);
        verify(roomMemberRepository, times(1)).getAll(user.getRoom());
        verify(hubService, times(1)).broadcast(new Message("", "user:list", new Gson().toJson(userLists), user.getRoom()));
    }

    @Test
    void update_unregister() {
        User user = new User("test-id", "test-name", "test-room");

        when(roomMemberRepository.getAll(user.getRoom())).thenReturn(new HashMap<String, RoomMember>());

        roomService.update(user, ACTION.UNREGISTER);

        verify(roomMemberRepository, times(1)).delete(user);
    }

    @Test
    void publish() {
        String nodeId = "node-id";
        Message message = new Message();
        message.setNodeId(nodeId);

        when(hubService.getNodeId()).thenReturn(nodeId);

        roomService.publish(message);

        verify(roomMemberRepository, times(1)).publish(message);
    }

    @Test
    void publish_otherNode() {
        Message message = new Message();
        message.setNodeId("node-id");

        when(hubService.getNodeId()).thenReturn("other-id");

        roomService.publish(message);

        verify(roomMemberRepository, never()).publish(message);
    }
}