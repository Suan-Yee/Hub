package com.example.demo.services.impl;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMessage;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupMessageRepository;
import com.example.demo.services.ChatRoomService;
import com.example.demo.services.GroupMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupMessageServiceImpl implements GroupMessageService {

    private GroupMessageRepository groupMessageRepository;

    @Autowired
    public GroupMessageServiceImpl(GroupMessageRepository groupMessageRepository) {
        this.groupMessageRepository = groupMessageRepository;
    }


    @Override
    public GroupMessage save(GroupMessage groupMessage) {

        return groupMessageRepository.save(groupMessage);
    }

    @Override
    public List<GroupMessage> findChatMessagesByGroup(Group group, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("time").descending());
        return groupMessageRepository.findMessageByGroup(group,pageable);
    }

    @Override
    public GroupMessage editMessage(Long messageId, String newContent) {
        GroupMessage groupMessage = groupMessageRepository.findById(messageId).orElse(null);
        if(groupMessage != null){
            groupMessage.setContent(newContent);
            return groupMessageRepository.save(groupMessage);
        }
        return null;
    }

    @Override
    public void deleteMessage(Long messageId) {
        GroupMessage groupMessage = groupMessageRepository.findById(messageId).orElse(null);
        if(groupMessage != null){
            groupMessageRepository.deleteById(messageId);
        }
    }

}
