package com.example.demo.services.impl;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatRoomRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Override
    public ChatRoom checkOrCreateChatRoom(Long senderId,Long recipientId) {
       Optional<ChatRoom> chatRoom = chatRoomRepository.findBySenderAndRecipientId(senderId,recipientId);

       if(chatRoom.isPresent()) {
           return chatRoom.get();
       }
        User sender = userRepository.findById(senderId).orElse(null);
        User recipient = userRepository.findById(recipientId).orElse(null);

        ChatRoom newChat = new ChatRoom();
        newChat.setSender(sender);
        newChat.setRecipient(recipient);

        return chatRoomRepository.save(newChat);
    }
    @Override
    public ChatRoom findByChatRoomId(String chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(chatRoomId).orElse(null);
        return chatRoom;
    }
}
