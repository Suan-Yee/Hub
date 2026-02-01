package com.example.demo.application.usecase;

import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Mention;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.util.List;

public interface MentionService {

    Mention saveNewMention(Post post,String user);
    List<MentionDto> getUserMentionList(Post post);
    Mention findByUser(User user);

    void deleteMentionUser(Long postId,List<Long> userIds);
}
