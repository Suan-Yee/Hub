package com.example.demo.controller.rest;

import com.example.demo.domain.ApiResponse;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.example.demo.security.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPost(
            @Valid @RequestBody CreatePostRequest postRequest,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long userId = extractUserId(authentication);
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
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long userId = extractUserId(authentication);
        PostResponse response = postService.getPostById(postId, userId);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Post retrieved successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse> getHomeFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request
    ) {
        Long userId = extractUserId(authentication);
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 50),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<PostResponse> feed = postService.getHomeFeed(userId, pageable);

        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Feed retrieved successfully",
            Map.of(
                "items", feed.getContent(),
                "page", feed.getNumber(),
                "size", feed.getSize(),
                "totalItems", feed.getTotalElements(),
                "totalPages", feed.getTotalPages(),
                "hasNext", feed.hasNext()
            )
        );

        return ResponseEntity.ok(apiResponse);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Authentication required");
        }
        return principal.getId();
    }
}
