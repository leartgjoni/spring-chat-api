package com.leartgjoni.springchatapi.repository;

import com.leartgjoni.springchatapi.model.User;
import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.model.RoomMember;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class RoomMemberRepository {

    final private HashOperations<String, String, RoomMember> hashOperations;
    final private RedisTemplate<String, Object> redisTemplate;

    public RoomMemberRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    public void create(User user) {
        hashOperations.put(getRoomNameFromUser(user), user.getId(), new RoomMember(user.getName()));
    }

    public Map<String, RoomMember> getAll(String roomName) {
        return hashOperations.entries(getRoomName(roomName));
    }

    public void delete(User user) {
        hashOperations.delete(getRoomNameFromUser(user), user.getId());
    }

    private String getRoomNameFromUser(User user) {
        return getRoomName(user.getRoom());
    }

    private String getRoomName(String roomName) {
        return "room_" + roomName;
    }

    public void publish(Message message) {
        redisTemplate.convertAndSend("room-messages", message);
    }
}
