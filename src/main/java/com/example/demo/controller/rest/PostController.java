package com.example.demo.controller.rest;

import com.example.demo.domain.ApiResponse;
import com.example.demo.dto.request.CreatePostForm;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * Create a new post
     * Uses @ModelAttribute for cleaner form binding
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createPost(
            @Valid @ModelAttribute CreatePostForm form,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) throws IOException {
        
        Long userId = extractUserId(userDetails);
        CreatePostRequest postRequest = form.toRequest();
        PostResponse response = postService.createPost(postRequest, userId);
        
        ApiResponse apiResponse = ApiResponse.created(
            request.getRequestURI(),
            "Post created successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    /**
     * Get a post by ID
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = extractUserId(userDetails);
        PostResponse response = postService.getPostById(postId, userId);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Post retrieved successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get posts by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPostsByUserId(userId, currentUserId, pageable);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "User posts retrieved successfully",
            Map.of(
                "posts", posts.getContent(),
                "pagination", Map.of(
                    "page", posts.getNumber(),
                    "size", posts.getSize(),
                    "totalElements", posts.getTotalElements(),
                    "totalPages", posts.getTotalPages()
                )
            )
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get public feed
     */
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPublicFeed(currentUserId, pageable);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Public feed retrieved successfully",
            Map.of(
                "posts", posts.getContent(),
                "pagination", Map.of(
                    "page", posts.getNumber(),
                    "size", posts.getSize(),
                    "totalElements", posts.getTotalElements(),
                    "totalPages", posts.getTotalPages()
                )
            )
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get posts by group ID
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse> getPostsByGroupId(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPostsByGroupId(groupId, currentUserId, pageable);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Group posts retrieved successfully",
            Map.of(
                "posts", posts.getContent(),
                "pagination", Map.of(
                    "page", posts.getNumber(),
                    "size", posts.getSize(),
                    "totalElements", posts.getTotalElements(),
                    "totalPages", posts.getTotalPages()
                )
            )
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Update a post
     * Uses @ModelAttribute for cleaner form binding
     */
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updatePost(
            @PathVariable Long postId,
            @Valid @ModelAttribute CreatePostForm form,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) throws IOException {
        
        Long userId = extractUserId(userDetails);
        
        // Convert form to request, but clear poll and group (cannot be updated)
        CreatePostRequest postRequest = new CreatePostRequest(
            form.getContent(),
            form.getTags(),
            form.getMentions(),
            form.getMediaFiles(),
            null, // Poll cannot be updated
            null, // Group cannot be changed
            form.getVisibility()
        );
        
        PostResponse response = postService.updatePost(postId, postRequest, userId);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Post updated successfully",
            Map.of("post", response)
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Delete a post
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = extractUserId(userDetails);
        postService.deletePost(postId, userId);
        
        ApiResponse apiResponse = ApiResponse.success(
            request.getRequestURI(),
            "Post deleted successfully"
        );
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Extract user ID from UserDetails
     * This is a placeholder - adjust based on your UserDetails implementation
     */
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        // TODO: Implement based on your UserDetails implementation
        // This might be something like:
        // return ((CustomUserDetails) userDetails).getId();
        // For now, returning a placeholder
        return 1L;
    }
}
