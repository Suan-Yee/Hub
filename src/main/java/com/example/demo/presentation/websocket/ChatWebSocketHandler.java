package com.example.demo.presentation.websocket;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.GroupMessageDto;
import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final GroupService groupService;
    private final GroupMessageService groupMessageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        User sender = userService.findById(Long.parseLong(chatMessage.getSenderId().toString()));
        User recipient = userService.findById(chatMessage.getRecipientId());
        chatMessage.setReceiver(sender);
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        ChatMessageDto dto = new ChatMessageDto(savedMsg.getContent(), savedMsg.getTime(), savedMsg.getRecipientId(), savedMsg.getReceiver().getId(), savedMsg.getChatId());
        messagingTemplate.convertAndSendToUser(recipient.getStaffId(), "/queue/messages", dto);
    }

    @MessageMapping("/group-chat")
    public void groupMessage(@Payload GroupMessageDto groupMessageDto) {
        User user = userService.findById(groupMessageDto.getSenderId());
        Group group = groupService.findGroupsByRoomId(groupMessageDto.getRoomId());

        String type = groupMessageDto.getType();
        String content = groupMessageDto.getContent();
        if (type != null && content != null && ("voice".equals(type) || "image".equals(type) || "video".equals(type))) {
            if (!content.startsWith("http://") && !content.startsWith("https://")) {
                throw new IllegalArgumentException(
                    "File messages must use REST upload first. POST /group-messages/upload, then send the returned URL via WebSocket.");
            }
        }

        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setUser(user);
        groupMessage.setGroup(group);
        groupMessage.setContent(groupMessageDto.getContent());
        groupMessage.setName(user.getName());
        groupMessage.setType(groupMessageDto.getType());

        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        groupMessage.setTime(date);

        GroupMessage savedMsg = groupMessageService.save(groupMessage);
        GroupMessageDto savedMsgDto = new GroupMessageDto(savedMsg);

        messagingTemplate.convertAndSend("/topic/group-messages/" + groupMessageDto.getRoomId(), savedMsgDto);
    }

    @MessageMapping("/edit-message")
    public void editMessage(@Payload GroupMessageDto groupMessageDto) {
        GroupMessage editedMessage = groupMessageService.editMessage(groupMessageDto.getMessageId(), groupMessageDto.getContent());
        GroupMessageDto editedMessageDto = new GroupMessageDto(editedMessage);
        messagingTemplate.convertAndSend("/topic/group-messages/edit/" + groupMessageDto.getRoomId(), editedMessageDto);
    }

    @MessageMapping("/delete-message")
    public void deleteMessage(@Payload GroupMessageDto groupMessageDto) {
        groupMessageService.deleteMessage(groupMessageDto.getMessageId());
        messagingTemplate.convertAndSend("/topic/group-messages/delete/" + groupMessageDto.getRoomId(), groupMessageDto.getMessageId());
    }

    @MessageMapping("/chat-withPhoto")
    public void processMessageWithPhoto(@Payload Map<String, Object> payload) {
        int roomId = Integer.parseInt(payload.get("id").toString());
        String senderStaffId = payload.get("sender").toString().trim();
        String content = payload.get("content").toString();
        User sender = userService.findByStaffId(senderStaffId);
        var roomOpt = chatRoomService.findById(roomId);
        if (roomOpt.isEmpty()) return;
        ChatRoom room = roomOpt.get();
        long recipientId = sender.getId().equals(room.getUser().getId()) ? (long) room.getRecipientId() : room.getUser().getId();
        User recipient = userService.findById(recipientId);
        ChatMessage msg = ChatMessage.builder()
                .content(content)
                .time(new Date())
                .recipientId(recipientId)
                .chatId(room.getChatId())
                .receiver(sender)
                .chatRoom(room)
                .build();
        ChatMessage savedMsg = chatMessageService.save(msg);
        ChatMessageDto dto = new ChatMessageDto(savedMsg.getContent(), savedMsg.getTime(), recipientId, sender.getId(), room.getChatId());
        messagingTemplate.convertAndSendToUser(recipient.getStaffId(), "/queue/messages", dto);
    }
}
