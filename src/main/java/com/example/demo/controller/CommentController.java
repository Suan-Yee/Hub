package com.example.demo.controller;


import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor @Slf4j
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostService postService;

    @MessageMapping("/comment/{postId}")
    @SendTo("/comment/messages/{postId}")
    public CommentDto send(@DestinationVariable Long postId, CommentDto commentDto, Principal principal){
        Comment comment = new Comment();
        User currentUser = userService.findByStaffId(principal.getName());
        User user = userService.findById(currentUser.getId());
        Post post = postService.findById(postId);
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(commentDto.getText());
        log.info("Comment {}",comment);
        Comment savedComment = commentRepository.save(comment);

        CommentDto commentDtoResponse = new CommentDto();
        commentDtoResponse.setId(savedComment.getId());
        commentDtoResponse.setUserId(savedComment.getUser().getId());
        commentDtoResponse.setText(savedComment.getText());
        commentDtoResponse.setPostId(savedComment.getPost().getId());
        return commentDtoResponse;
    }

}
