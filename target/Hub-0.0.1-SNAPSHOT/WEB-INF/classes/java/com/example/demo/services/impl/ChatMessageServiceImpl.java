package com.example.demo.services.impl;

import com.cloudinary.Cloudinary;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.services.ChatMessageService;
import com.example.demo.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    public ChatRoomService chatRoomService;
    private final List<String> photoExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", "bmp","tiff","tif","psv","svg","webp","ico","heic");
    private final Cloudinary cloudinary;

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

    @Override
    public ChatMessage saveWithAttachment(Long id, MultipartFile file, String sender, String date) throws IOException {
        if(isValidPhotoExtension(getFileExtension(file))){
            ChatMessage chatMessage = ChatMessage.builder()
                    .chatRoom(ChatRoom.builder().id(Math.toIntExact(id)).build())
                    .time(new Date())
                    .content(uploadPhoto(file))
                    .build();
            chatMessageRepository.save(chatMessage);
            return chatMessage;
        }else {
            return null;
        }
    }
    public boolean isValidPhotoExtension(String extension) {
        return photoExtensions.contains(extension);
    }

    public String getFileExtension(MultipartFile file){
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.')).toLowerCase();
    }

    public String uploadPhoto(MultipartFile file) throws IOException {
        return cloudinary.uploader()
                .upload(file.getBytes(), Map.of( "public_id", UUID.randomUUID().toString()))
                .get("url").toString();
    }
}