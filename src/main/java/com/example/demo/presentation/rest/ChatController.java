package com.example.demo.presentation.rest;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.application.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final GroupService groupService;
    private final GroupMessageService groupMessageService;
    private final UserRoomService userRoomService;
    private final FileUploadService fileUploadService;
    private final OnlineStatusService onlineStatusService;

    @GetMapping("/online-users")
    public ResponseEntity<Set<String>> getOnlineStaffIds() {
        return ResponseEntity.ok(onlineStatusService.getOnlineStaffIds());
    }

    @GetMapping("/online-users/{staffId}")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable String staffId) {
        return ResponseEntity.ok(onlineStatusService.isOnline(staffId));
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

    @PostMapping("/send-photo-toChatRoom")
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
