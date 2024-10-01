package com.example.demo.services.impl;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.CommentLike;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.services.CommentLikeService;
import com.example.demo.services.CommentService;
import com.example.demo.services.NotificationService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
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

    @Override
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
            notificationService.notifyUser(commentOwner.getStaffId(), user.getName() + " like your comment");
            }
            return comment.isLikeStatus();
        }else{
            CommentLike newCommentLike = createCommentLike(likeDto,principal);
            CommentLike commentLike1 = commentLikeRepository.save(newCommentLike);
            log.info("Comment Owner {}",commentOwner);
            if(!Objects.equals(commentLike1.getUser().getStaffId(), user.getStaffId())){
            notificationService.notifyUser(commentOwner.getStaffId(), user.getName() + " like your comment");
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
