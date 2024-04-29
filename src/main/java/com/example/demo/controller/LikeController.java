package com.example.demo.controller;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.Like;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.form.UserCommentForm;
import com.example.demo.services.LikeService;
import com.example.demo.services.NotificationService;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
@Slf4j
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;

    @PostMapping("/like")
    public ResponseEntity<?> likePost(@RequestBody LikeDto likeDto, Principal principal) {
        try {
            boolean isLiked = likeService.saveLike(likeDto, principal);
            return ResponseEntity.ok(Map.of("liked", isLiked));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to like the post");
        }
    }
    @GetMapping("/userId")
    public ResponseEntity<?> userId(Principal principal){
        User user = userService.findByStaffId(principal.getName());
        UserCommentForm form = UserCommentForm.builder().id(user.getId())
                .name(user.getName()).image(user.getPhoto()).build();
        return new ResponseEntity<>(form, HttpStatus.OK);
    }
}
