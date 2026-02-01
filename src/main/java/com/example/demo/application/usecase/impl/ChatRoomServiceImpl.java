package com.example.demo.application.usecase.impl;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatRoomRequest;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.ChatRoomRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.application.usecase.ChatRoomService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private UserService userService;

//    @Override
//    public ChatRoom checkOrCreateChatRoom(Long senderId,Long recipientId) {
//       Optional<ChatRoom> chatRoom = chatRoomRepository.findBySenderAndRecipientId(senderId,recipientId);
//
//       if(chatRoom.isPresent()) {
//           return chatRoom.get();
//       }
//        User sender = userRepository.findById(senderId).orElse(null);
//        User recipient = userRepository.findById(recipientId).orElse(null);
//
//        ChatRoom newChat = new ChatRoom();
//        newChat.setSender(sender);
//        newChat.setRecipient(recipient);
//
//        return chatRoomRepository.save(newChat);
//    }
//    @Override
//    public ChatRoom findByChatRoomId(String chatRoomId){
//        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId(chatRoomId).orElse(null);
//        return chatRoom;
//    }

//    @Transactional
//    @Override
//    public ChatRoom save(String name) {
//        ChatRoom chatRoom = ChatRoom.builder()
//                .date(new Date())
//                .name(name)
//                .build();
//        return chatRoomRepository.save(chatRoom);
//    }

//    @Override
//    public ChatRoom findById(Long id) {
//        return chatRoomRepository.findById(id).orElseThrow();
//    }

    @Override
    public Optional<String> getChatRoomId(User senderId, Long recipientId, boolean createNewRoomIfNotExists) {
        if (senderId == null || recipientId == 0) {
            throw new IllegalArgumentException("Sender ID and Recipient ID must not be null or empty.");
        }

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUserAndRecipientId(senderId, Math.toIntExact(recipientId));
        if (existingRoom.isPresent()) {
            return Optional.of(existingRoom.get().getChatId());
        }

        if (createNewRoomIfNotExists) {
            String chatId = createChatId(senderId.getId(),recipientId);
            return Optional.of(chatId);
        }

        return Optional.empty();
    }

    @Override
    public Optional<ChatRoom> findById(int roomId) {
        return chatRoomRepository.findById((long) roomId);
    }

    private String createChatId(Long senderId, Long recipientId) {
        String chatId = String.format("%s_%s", senderId, recipientId);
        ChatRoom senderRecipient = new ChatRoom();
        User user=userService.findById(senderId);
        senderRecipient.setChatId(chatId);
        senderRecipient.setUser(user);
        senderRecipient.setRecipientId(Math.toIntExact(recipientId));

        ChatRoom recipientSender = new ChatRoom();
        recipientSender.setChatId(chatId);
        User user1=userService.findById(recipientId);
        recipientSender.setUser(user1);
        recipientSender.setRecipientId(Math.toIntExact(senderId));

        chatRoomRepository.save(senderRecipient);
        chatRoomRepository.save(recipientSender);

        return chatId;
    }
}
