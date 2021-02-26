package com.leartgjoni.springchatapi.config;

import com.leartgjoni.springchatapi.model.Message;
import com.leartgjoni.springchatapi.service.HubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public class RedisMessageReceiver implements MessageListener {
    final private Jackson2JsonRedisSerializer<Message> serializer = new Jackson2JsonRedisSerializer<>(Message.class);

    @Autowired
    private HubService hubService;

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message rawMessage, byte[] bytes) {
        Message message = serializer.deserialize(rawMessage.getBody());
        if (message == null) return;

        // only broadcast messages coming from other nodes
        if (!message.getNodeId().equals(hubService.getNodeId())) {
            hubService.broadcast(message);
        }
    }
}
