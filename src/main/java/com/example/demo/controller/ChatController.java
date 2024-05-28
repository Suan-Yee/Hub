package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.dto.GroupDto;
import com.example.demo.dto.GroupMessageDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    private final UserHasGroupService userHasGroupService;


    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        User user = userService.findById(Long.parseLong(chatMessage.getSenderId().toString()));
        chatMessage.setReceiver(user);
        ChatMessage savedMsg = chatMessageService.save(chatMessage);
        ChatMessageDto chatMessageDto = new ChatMessageDto(savedMsg.getContent(), savedMsg.getTime(), savedMsg.getRecipientId(), savedMsg.getReceiver().getId(), savedMsg.getChatId());
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId().toString(), "/queue/messages",
                chatMessageDto
        );
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
    public void groupMessage(@Payload GroupMessageDto groupMessageDto) {
        User user = userService.findById(groupMessageDto.getSenderId());
        Group group = groupService.findGroupsByRoomId(groupMessageDto.getRoomId());

        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setUser(user);
        groupMessage.setGroup(group);
        groupMessage.setContent(groupMessageDto.getContent());
        groupMessage.setName(user.getName());

        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        groupMessage.setTime(date);

        GroupMessage savedMsg = groupMessageService.save(groupMessage);
        GroupMessageDto savedMsgDto = new GroupMessageDto(savedMsg.getContent(), savedMsg.getTime(), savedMsg.getUser().getId(), savedMsg.getGroup().getId(), savedMsg.getName());

        messagingTemplate.convertAndSend("/topic/group-messages/" + groupMessageDto.getRoomId(), savedMsgDto);
    }


    @GetMapping("/messages/{id}")
    public ResponseEntity<List<GroupMessageDto>> findChatMessages(@PathVariable("id") Long roomId) {
        Group group = groupService.findGroupsByRoomId(roomId);
        List<GroupMessage> groupMessages = groupMessageService.findChatMessagesByGroup(group);
        List<GroupMessageDto> groupMessageDtos = groupMessages.stream()
                .map(message -> new GroupMessageDto(
                        message.getContent(),
                        message.getTime(),
                        message.getUser().getId(),
                        message.getGroup().getId(),
                        message.getName()
                ))
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
}


