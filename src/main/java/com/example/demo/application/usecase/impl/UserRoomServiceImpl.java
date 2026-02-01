package com.example.demo.application.usecase.impl;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRoom;
import com.example.demo.infrastructure.persistence.repository.ChatRoomRepository;
import com.example.demo.infrastructure.persistence.repository.UserRepository;
import com.example.demo.infrastructure.persistence.repository.UserRoomRepository;
import com.example.demo.application.usecase.UserRoomService;
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
