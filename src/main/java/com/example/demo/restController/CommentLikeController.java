package com.example.demo.restController;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.CommentLike;
import com.example.demo.services.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping ("/like")
    public ResponseEntity<?> saveLike(@RequestBody LikeDto likeDto, Principal principal){
        try{
            boolean isLiked = commentLikeService.saveLikeForComment(likeDto,principal);
            return ResponseEntity.ok(Map.of("liked", isLiked));
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to like the post");
        }
    }
}
