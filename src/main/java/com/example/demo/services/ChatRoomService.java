package com.example.demo.services;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface ChatRoomService {

    ChatRoom checkOrCreateChatRoom(Long senderId,Long recipientId);
    ChatRoom findByChatRoomId(String chatRoomId);
}
