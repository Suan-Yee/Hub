package com.example.demo.controller.rest;

import com.example.demo.domain.ApiResponse;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPost(
            @Valid @RequestBody CreatePostRequest postRequest,
//            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        
//        Long userId = extractUserId(userDetails);
        Long userId = 1L;
        PostResponse response = postService.createPost(postRequest, userId);
        
        ApiResponse apiResponse = ApiResponse.created(
            request.getRequestURI(),
            "Post created successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse> getPostById(
            @PathVariable Long postId,
//            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = 1L;
        PostResponse response = postService.getPostById(postId, userId);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Post retrieved successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.ok(apiResponse);
    }
}
