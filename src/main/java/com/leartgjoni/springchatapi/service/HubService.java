package com.leartgjoni.springchatapi.service;

import com.leartgjoni.springchatapi.config.RedisMessageReceiver;
import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
public class HubService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMessageReceiver.class);

    final private String nodeId;

    @Autowired
    RoomService roomService;

    final private Map<String, Map<String, User>> rooms = new ConcurrentHashMap<>();

    public HubService() {
        this.nodeId = UUID.randomUUID().toString();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void register(User user) {
        Map<String, User> room = rooms.get(user.getRoom());
        if (room == null) {
            room = new ConcurrentHashMap<>();
            rooms.put(user.getRoom(), room);
        }
        room.put(user.getId(), user);

        roomService.update(user, ACTION.REGISTER);
    }

    public void unregister(User user) {
        Map<String, User> room = rooms.get(user.getRoom());
        if (room != null) {
            room.remove(user.getId());
        }

        roomService.update(user, ACTION.UNREGISTER);
    }

    public void broadcast(Message message) {
        if (message.getNodeId() == null)
            message.setNodeId(this.nodeId);

        roomService.publish(message);

        Map<String, User> room = rooms.get(message.getRoom());

        for (User user : room.values()) {
            if (user.getId().equals(message.getUserId())) continue;

            try {
                user.getWsSession().sendMessage(new TextMessage(message.toJSON()));
            } catch (IOException exception) {
                LOGGER.error(exception.toString());
            }
        }
    }
}
