package com.example.demo.services;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatMessageService {

    public ChatMessage save(ChatMessage chatMessage);
//    public List<ChatMessage> findChatMessagesByRoomId(Long id);

    List<ChatMessage> findChatMessages(User senderId, Long recipientId);
}
