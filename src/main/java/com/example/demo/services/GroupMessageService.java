package com.example.demo.services;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMessage;
import com.example.demo.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GroupMessageService {

     GroupMessage save(GroupMessage groupMessage);
    List<GroupMessage> findChatMessagesByGroup(Group group);

}
