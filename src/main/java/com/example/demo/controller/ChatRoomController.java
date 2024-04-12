package com.example.demo.controller;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.ChatRoomRequest;
import com.example.demo.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor @Slf4j
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    @PostMapping("/checkRoom")
    @ResponseBody
    public ResponseEntity<?> checkToCreate(@RequestBody ChatRoomRequest chatRoomRequest){
        ChatRoom chatRoom = chatRoomService.checkOrCreateChatRoom(chatRoomRequest.getSenderId(),chatRoomRequest.getRecipientId());
        Map<String, String> responseBody = Collections.singletonMap("chatRoomId", chatRoom.getChatRoomId());
        return ResponseEntity.ok(responseBody);
    }
    @GetMapping("/chat-room/{roomId}")
    public String chatRoom(@PathVariable("roomId") String roomId, Model model) {
        model.addAttribute("roomId", roomId);
        ChatRoom chatRoom = chatRoomService.findByChatRoomId(roomId);
        Long recipientId = chatRoom.getRecipient().getId();
        model.addAttribute("recipientId",recipientId);
        log.info("Chat room id {} ",roomId);
        return "privateChat";
    }
}
