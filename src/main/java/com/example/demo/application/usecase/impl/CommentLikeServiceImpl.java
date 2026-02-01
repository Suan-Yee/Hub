package com.example.demo.application.usecase.impl;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.CommentLike;
import com.example.demo.entity.User;
import com.example.demo.infrastructure.persistence.repository.CommentLikeRepository;
import com.example.demo.application.event.NotificationReadyEvent;
import com.example.demo.application.usecase.CommentLikeService;
import com.example.demo.application.usecase.CommentService;
import com.example.demo.application.usecase.NotificationService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Objects;

@Service
@RequiredArgsConstructor @Slf4j
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentService commentService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean saveLikeForComment(LikeDto likeDto,Principal principal) {

        User user = userService.findAuthenticatedUser(principal);
        CommentLike commentLike = commentLikeRepository.findLikeComment(likeDto.getCommentId(),user.getId()).orElse(null);
        Comment mainComment = commentService.findById(likeDto.getCommentId());
        User commentOwner =  mainComment.getUser();

        if(commentLike != null){
            commentLike.setLikeStatus(!commentLike.isLikeStatus());
            CommentLike comment = commentLikeRepository.save(commentLike);
            log.info("Comment Owner {}",commentOwner);
            if(!Objects.equals(comment.getUser().getStaffId(), user.getStaffId())){
            eventPublisher.publishEvent(new NotificationReadyEvent(commentOwner.getStaffId(), user.getName() + " like your comment"));
            }
            return comment.isLikeStatus();
        }else{
            CommentLike newCommentLike = createCommentLike(likeDto,principal);
            CommentLike commentLike1 = commentLikeRepository.save(newCommentLike);
            log.info("Comment Owner {}",commentOwner);
            if(!Objects.equals(commentLike1.getUser().getStaffId(), user.getStaffId())){
            eventPublisher.publishEvent(new NotificationReadyEvent(commentOwner.getStaffId(), user.getName() + " like your comment"));
            }
            return commentLike1.isLikeStatus();
        }
    }

    @Override
    public void deleteByCommentId(Long commentId) {
       if(commentLikeRepository.existsById(commentId)){
           commentLikeRepository.deleteById(commentId);
       }
       return;
    }

    private CommentLike createCommentLike(LikeDto likeDto, Principal principal){

        CommentLike commentLike = new CommentLike();
        Comment comment = commentService.findById(likeDto.getCommentId());
        User user = userService.findAuthenticatedUser(principal);
        commentLike.setComment(comment);
        commentLike.setUser(user);
        commentLike.setLikeStatus(true);

        return commentLike;
    }
}
