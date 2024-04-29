package com.example.demo.controller;


import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.CommentService;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @MessageMapping("/comment/{postId}")
    @SendTo("/comment/messages/{postId}")
    public CommentDto send(@DestinationVariable Long postId, CommentDto commentDto, Principal principal){

        User user = userService.findAuthenticatedUser(principal);
        Post post = postService.findById(postId);

        Comment savedComment = commentService.createComment(user,post,commentDto);

        CommentDto comment = commentService.createCommentDto(savedComment,user);
        comment.setTime(formatTimeAgo(savedComment.getCreatedAt()));
        return comment;
    }

    @GetMapping("/post/comment")
    @ResponseBody
    public ResponseEntity<?> getAllComment(@RequestParam(name = "postId",required = false) Long postId){
        List<CommentDto> commentList = commentService.fetchAllComment(postId);
        if (commentList != null){
            return new ResponseEntity<>(commentList, HttpStatus.OK);
        }else{
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
    public ResponseEntity<String> editComment(@RequestParam(name = "commentId")Long commentId,@RequestBody CommentDto commentDto){
        try{
            commentService.editComment(commentId,commentDto.getText());
            log.info("edit text {}",commentDto.getText());
            return ResponseEntity.ok().build();
        }catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update comment");
        }
    }

    @DeleteMapping("/comment/delete")
    @ResponseBody
    public ResponseEntity<String> deleteCommentById(@RequestParam(name = "commentId") Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete comment");
        }
    }
}
