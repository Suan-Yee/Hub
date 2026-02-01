package com.example.demo.application.usecase;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.UserRoom;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserRoomService {

//    public List<UserRoom> findByUserId(Long id);
//    public void createdRoom(String name,List<Long> selectedUserIds);

    public List<UserRoom> findByChatRoomId(Long id);
}
