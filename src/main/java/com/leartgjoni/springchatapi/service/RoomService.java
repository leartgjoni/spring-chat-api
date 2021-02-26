package com.leartgjoni.springchatapi.service;

import com.google.gson.Gson;
import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.repository.RoomMemberRepository;
import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.RoomMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

enum ACTION {
        REGISTER,
        UNREGISTER
        }

@Service
public class RoomService {
    final private Gson gson;

    @Autowired
    RoomMemberRepository roomMemberRepository;

    @Autowired
    HubService hubService;

    public RoomService() {
        gson = new Gson();
    }

    public void update(User user, ACTION action) {
        if (action == ACTION.UNREGISTER)
            roomMemberRepository.delete(user);
        else if (action == ACTION.REGISTER)
            roomMemberRepository.create(user);

        Map<String, RoomMember> userLists = roomMemberRepository.getAll(user.getRoom());
        hubService.broadcast(new Message("", "user:list", gson.toJson(userLists), user.getRoom()));
    }

    public void publish(Message message) {
        // pub to redis only messages coming from this node
        if (!message.getNodeId().equals(hubService.getNodeId())) return;

        roomMemberRepository.publish(message);
    }
}
