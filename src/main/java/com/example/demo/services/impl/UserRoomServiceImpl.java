//package com.example.demo.services.impl;
//
//import com.example.demo.entity.ChatRoom;
//import com.example.demo.entity.UserRoom;
//import com.example.demo.repository.ChatRoomRepository;
//import com.example.demo.repository.UserRoomRepository;
//import com.example.demo.services.ChatRoomService;
//import com.example.demo.services.UserRoomService;
//import com.example.demo.services.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class UserRoomServiceImpl implements UserRoomService {
//
//    private final ChatRoomRepository chatRoomRepo;
//    private final UserRoomRepository userRoomRepository;
//    private final UserService userService;
//    private final ChatRoomService chatRoomService;
//
//    @Override
//    public List<UserRoom> findByUserId(Long id) {
//        return userRoomRepository.findByUserId(id);
//    }
//
//    @Transactional
//    @Override
//    public void createdRoom(String name, List<Long> selectedUserIds) {
//        var chatRoom = chatRoomService.save(name);
//        for (Long id : selectedUserIds) {
//            var user = userService.findById(id);
//            UserRoom user_chatRoom = UserRoom.builder()
//                    .chatRoom(chatRoom)
//                    .date(new Date())
//                    .user(user)
//                    .build();
//            userRoomRepository.save(user_chatRoom);
//        }
//    }
//}
