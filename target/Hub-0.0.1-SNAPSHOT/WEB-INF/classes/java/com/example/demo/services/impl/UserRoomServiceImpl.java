package com.example.demo.services.impl;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRoom;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoomRepository;
import com.example.demo.services.UserRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoomServiceImpl implements UserRoomService {

    private final UserRoomRepository userRoomRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserRoom> findByChatRoomId(Long id) {
        return userRoomRepository.findByChatRoomId(id);
    }


}
