package com.example.demo.presentation.rest;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
import com.example.demo.utils.Base64Multipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    public final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final GroupService groupService;
    private final GroupMessageService groupMessageService;
    private final UserRoomService userRoomService;
    private final FileUploadService fileUploadService;
    private final UserHasGroupService userHasGroupService;
    private final OnlineStatusService onlineStatusService;

    @GetMapping("/online-users")
    @ResponseBody
    public ResponseEntity<Set<String>> getOnlineStaffIds() {
        return ResponseEntity.ok(onlineStatusService.getOnlineStaffIds());
    }

    @GetMapping("/online-users/{staffId}")
    @ResponseBody
    public ResponseEntity<Boolean> isUserOnline(@PathVariable String staffId) {
        return ResponseEntity.ok(onlineStatusService.isOnline(staffId));
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        User sender = userService.findById(Long.parseLong(chatMessage.getSenderId().toString()));
        User recipient = userService.findById(chatMessage.getRecipientId());
        chatMessage.setReceiver(sender);
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        ChatMessageDto dto = new ChatMessageDto(savedMsg.getContent(), savedMsg.getTime(), savedMsg.getRecipientId(), savedMsg.getReceiver().getId(), savedMsg.getChatId());
        messagingTemplate.convertAndSendToUser(recipient.getStaffId(), "/queue/messages", dto);
    }

    @GetMapping("/messages/{senderId}/{selectedUserId}")
    public ResponseEntity<List<ChatMessageDto>> findChatMessages(@PathVariable String senderId,
                                                                 @PathVariable String selectedUserId) {
        User user = userService.findById(Long.parseLong(senderId));
        List<ChatMessage> messages = chatMessageService.findChatMessages(user, Long.parseLong(selectedUserId));
        List<ChatMessageDto> chatMessageDto = messages.stream().map(message -> new ChatMessageDto(
                message.getContent(),
                message.getTime(),
                message.getRecipientId(),
                message.getReceiver().getId(),
                message.getChatId()
        )).collect(Collectors.toList());


        return ResponseEntity
                .ok(chatMessageDto);
    }


    @GetMapping("/group-users")
    public ResponseEntity<List<UserDto>> findConnectedUsers() {
        List<User> users = userService.findByStatus(true);
        List<UserDto> userDtos = users.stream()
                .map(user -> new UserDto(
                        user.getStaffId(),
                        user.getName(),
                        user.getDepartment(),
                        user.getRole(),
                        user.getPhoto(),
                        user.getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);

    }

    @GetMapping("/roomList")
    @ResponseBody
    public ResponseEntity<List<GroupDto>> groupList(Principal principal) {
        User user = userService.findByStaffId(principal.getName());
        List<Long> groupIds = groupService.findGroupIdsByUserId(user.getId());
        System.out.println(groupIds);
        List<GroupDto> groupDtoList = new ArrayList<>();
        for (Long group : groupIds) {
            System.out.println(group);
            GroupDto groups = groupService.findGroupsByIds(group);
            System.out.println(groups);
            groupDtoList.add(groups);
        }

        return ResponseEntity.ok(groupDtoList);
    }

    @MessageMapping("/group-chat")
    public void groupMessage(@Payload GroupMessageDto groupMessageDto) throws IOException {
        User user = userService.findById(groupMessageDto.getSenderId());
        Group group = groupService.findGroupsByRoomId(groupMessageDto.getRoomId());

        if (groupMessageDto.getType() != null && groupMessageDto.getContent() != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(groupMessageDto.getContent());
            MultipartFile multipartFile;
            String uploadedUrl;

            if (groupMessageDto.getType().equals("voice")) {
                multipartFile = new Base64Multipart(decodedBytes, "data:audio/wav;base64");
                uploadedUrl = fileUploadService.uploadVoice(multipartFile);
            } else if (groupMessageDto.getType().equals("image")) {
                multipartFile = new Base64Multipart(decodedBytes, "data:image/jpeg;base64");
                uploadedUrl = fileUploadService.uploadFile(multipartFile);
            } else if (groupMessageDto.getType().equals("video")) {
                multipartFile = new Base64Multipart(decodedBytes, "data:video/mp4;base64");
                uploadedUrl = fileUploadService.uploadFile(multipartFile);
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + groupMessageDto.getType());
            }

            groupMessageDto = groupMessageDto.withContent(uploadedUrl);
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
    public ResponseEntity<GroupMessageDto> editMessage(@RequestBody GroupMessageDto groupMessageDto) {
        log.info("Reached to editMessage");
        GroupMessage editedMessage = groupMessageService.editMessage(groupMessageDto.getMessageId(), groupMessageDto.getContent());
        GroupMessageDto editedMessageDto = new GroupMessageDto(editedMessage);
        messagingTemplate.convertAndSend("/topic/group-messages/edit/" + groupMessageDto.getRoomId(), editedMessageDto);
        return ResponseEntity.ok(editedMessageDto);
    }

    @MessageMapping("/delete-message")
    public ResponseEntity<Void> deleteMessage(@RequestBody GroupMessageDto groupMessageDto) {
        groupMessageService.deleteMessage(groupMessageDto.getMessageId());
        messagingTemplate.convertAndSend("/topic/group-messages/delete/" + groupMessageDto.getRoomId(), groupMessageDto.getMessageId());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/messages/{id}")
    public ResponseEntity<List<GroupMessageDto>> findChatMessages(@PathVariable("id") Long roomId, @RequestParam("page") int page, @RequestParam("size") int size) {
        Group group = groupService.findGroupsByRoomId(roomId);
        List<GroupMessage> groupMessages = groupMessageService.findChatMessagesByGroup(group, page, size);
        List<GroupMessageDto> groupMessageDtos = groupMessages.stream()
                .map(GroupMessageDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groupMessageDtos);
    }
    @GetMapping("/room-member-size/{id}")
    @ResponseBody
    public ResponseEntity<?> getChatRoomSize(@PathVariable("id") Long id) {
        var user_chat_room = userRoomService.findByChatRoomId(id);
        List<UserRoom> userChatRooms = new ArrayList<>();
        for(UserRoom user_chatRoom:user_chat_room){
            if(!user_chatRoom.getUser().getId().equals(999)){
                userChatRooms.add(user_chatRoom);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userChatRooms.size());
    }
    @GetMapping("/chat-room-memberList/{id}")
    @ResponseBody
    public ResponseEntity<List<User>> getAllUsersForChatRoom(@PathVariable("id") Long id) {
        List<UserRoom> user_chatRooms = userRoomService.findByChatRoomId(id);
        List<User> userList = new ArrayList<>();
        for (UserRoom user_chatRoom : user_chatRooms) {
            var user = userService.findById(user_chatRoom.getUser().getId());
            if(!user.getRole().equals(User.builder())){
                userList.add(user);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userList);
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
    @PostMapping("/send-photo-toChatRoom")
    @ResponseBody
    public ResponseEntity<ChatMessage> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                        @RequestParam("id") Long id,
                                                        @RequestParam("sender") String sender,
                                                        @RequestParam("date") String date) throws IOException {
        ChatMessage chatMessage = chatMessageService.saveWithAttachment(id, file, sender, date);
        if (chatMessage != null) {
            chatRoomService.findById(id.intValue()).ifPresent(room -> {
                User senderUser = userService.findByStaffId(sender);
                long recipientId = senderUser.getId().equals(room.getUser().getId()) ? (long) room.getRecipientId() : room.getUser().getId();
                User recipient = userService.findById(recipientId);
                ChatMessageDto dto = new ChatMessageDto(chatMessage.getContent(), chatMessage.getTime(), recipientId, senderUser.getId(), room.getChatId());
                messagingTemplate.convertAndSendToUser(recipient.getStaffId(), "/queue/messages", dto);
            });
        }
        return ResponseEntity.status(HttpStatus.OK).body(chatMessage);
    }
}
