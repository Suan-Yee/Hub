package com.example.demo.application.usecase;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatRoomRequest;
import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface ChatRoomService {

//    ChatRoom checkOrCreateChatRoom(Long senderId,Long recipientId);
//    ChatRoom findByChatRoomId(String chatRoomId);

//    public ChatRoom save(String name);
//    public ChatRoom findById(Long id);

    Optional<String> getChatRoomId(User senderId,Long recipientId, boolean createNewRoomIfNotExists);

    Optional<ChatRoom> findById(int roomId);
}
