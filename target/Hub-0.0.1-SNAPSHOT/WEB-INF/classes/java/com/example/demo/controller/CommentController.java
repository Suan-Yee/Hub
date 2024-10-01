package com.example.demo.controller;


import com.example.demo.dto.CommentDto;
import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Controller
@RequiredArgsConstructor @Slf4j
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;

    @MessageMapping("/comment/{postId}")
    @SendTo("/comment/messages/{postId}")
    public CommentDto send(@DestinationVariable Long postId, CommentDto commentDto, Principal principal){

        User user = userService.findAuthenticatedUser(principal);
        Post post = postService.findById(postId);

        Comment savedComment = commentService.createComment(user,post,commentDto);
        CommentDto comment = commentService.createCommentDto(savedComment,user);

        if(!commentDto.getMention().isEmpty()){
            List<MentionDto> mentionDtoList= commentService.saveMentionNotificationAndUser(commentDto,savedComment);
            comment.setMentionUserList(mentionDtoList);
        }
        comment.setTime(formatTimeAgo(savedComment.getCreatedAt()));
        comment.setOwner(true);
        return comment;
    }

    @GetMapping("/post/comments")
    @ResponseBody
    public ResponseEntity<?> getAllComments(
            @RequestParam(name = "postId", required = true) Long postId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<CommentDto> commentPage = commentService.fetchAllComment(page, size, postId);

        if (commentPage.hasContent()) {
            return new ResponseEntity<>(commentPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/post/reply")
    @ResponseBody
    public ResponseEntity<?> findByParentId(@RequestParam(name = "parentId",required = false)Long parentId){
        List<CommentDto> commentList = commentService.findByParentId(parentId);

        if(commentList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(commentList,HttpStatus.OK);
        }
    }
    @PutMapping("/comment/edit")
    @ResponseBody
    public ResponseEntity<CommentDto> editComment(@RequestParam(name = "commentId")Long commentId,@RequestBody CommentDto commentDto){
        try{
            CommentDto newComment = commentService.editComment(commentId,commentDto.getText(),commentDto.getMention());
            log.info("edit text {}",commentDto.getText());
            return new ResponseEntity<>(newComment,HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/comment/delete")
    @ResponseBody
    public ResponseEntity<String> deleteCommentById(@RequestParam(name = "commentId") Long commentId) {
        try {
//            notificationService.deleteNotificationByCommentId(commentId);
            commentService.deleteComment(commentId);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete comment");
        }
    }
}
