package com.example.demo.application.usecase;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public interface ChatMessageService {

    public ChatMessage save(ChatMessage chatMessage);
//    public List<ChatMessage> findChatMessagesByRoomId(Long id);

    List<ChatMessage> findChatMessages(User senderId, Long recipientId);

    List<ChatMessage> findChatMessages(User senderId, Long recipientId, int page, int size);
    public ChatMessage saveWithAttachment(Long id, MultipartFile file, String sender, String date) throws IOException;
}
