package com.example.demo.controller;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

//    @MessageMapping("/chat/{chatRoomId}")
//    @SendTo("/topic/messages/{chatRoomId}")
//    public ChatMessage sendMessage(@DestinationVariable String chatRoomId,ChatMessage chatMessage){
//
//        ChatRoom chatRoom = chatRoomService.findByChatRoomId(chatRoomId);
//        chatMessage.setChatRoom(chatRoom);
//
//        if (chatMessage.getSenderId() != null) {
//            User sender = userRepository.findById(chatMessage.getSenderId()).orElse(null);
//            chatMessage.setSender(sender);
//        }
//        if (chatMessage.getRecipientId() != null) {
//            User receiver = userRepository.findById(chatMessage.getRecipientId()).orElse(null);
//            chatMessage.setReceiver(receiver);
//        }
//        chatMessage.setTimestamp(LocalDateTime.now());
//        chatMessageRepository.save(chatMessage);
//        return chatMessage;
//    }
}
