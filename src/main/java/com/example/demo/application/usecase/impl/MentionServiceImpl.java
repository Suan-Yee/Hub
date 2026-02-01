package com.example.demo.application.usecase.impl;

import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Mention;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.MentionRepository;
import com.example.demo.application.event.NotificationReadyEvent;
import com.example.demo.application.usecase.MentionService;
import com.example.demo.application.usecase.NotificationService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final UserService userService;
    private final MentionRepository mentionRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Mention saveNewMention(Post post,String user) {
        User result = userService.findByStaffId(user);
        Mention mention = new Mention();
        mention.setPost(post);
        mention.setUser(result);
        mentionRepository.save(mention);
        String message = post.getUser().getName() + " tagged u in his post";
        eventPublisher.publishEvent(new NotificationReadyEvent(user, message));
        User userResult = userService.findByStaffId(user);
        notificationService.createNotificationForTag(message,post,userResult,true);
        return mention;
    }

    @Override
    public List<MentionDto> getUserMentionList(Post post) {
        List<Mention> userList = mentionRepository.getAllUserByMention(post.getId());
        return userList.stream().map(MentionDto::new).toList();
    }
    @Override
    public Mention findByUser(User user){
        return mentionRepository.findByUser(user);
    }

    @Override
    public void deleteMentionUser(Long postId, List<Long> userId) {
        for(Long user : userId){
            mentionRepository.deleteMentionUser(postId,user);
            notificationService.deleteTagNotification(user,postId);
        }
    }
}
