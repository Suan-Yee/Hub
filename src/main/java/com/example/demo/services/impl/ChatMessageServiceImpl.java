package com.example.demo.services.impl;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.services.ChatMessageService;
import com.example.demo.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    public ChatRoomService chatRoomService;

    @Transactional
    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
        return chatMessage;
    }

//    @Override
//    public List<ChatMessage> findChatMessagesByRoomId(Long id) {
//        return chatMessageRepository.findChatMessagesByChatRoomId(id);
//    }

    @Override
    public List<ChatMessage> findChatMessages(User senderId, Long recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(chatMessageRepository::findByChatId).orElse(new ArrayList<>());
    }
}
