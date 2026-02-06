package com.example.demo.controller.rest;

import com.example.demo.dto.request.CreatePostForm;
import com.example.demo.dto.request.CreatePostRequest;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.PostService;
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
    public ResponseEntity<PostResponse> createPost(
            @Valid @ModelAttribute CreatePostForm form,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        
        Long userId = extractUserId(userDetails);
        CreatePostRequest request = form.toRequest();
        PostResponse response = postService.createPost(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a post by ID
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        PostResponse response = postService.getPostById(postId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get posts by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPostsByUserId(userId, currentUserId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get public feed
     */
    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getPublicFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPublicFeed(currentUserId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get posts by group ID
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<PostResponse>> getPostsByGroupId(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long currentUserId = extractUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> posts = postService.getPostsByGroupId(groupId, currentUserId, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Update a post
     * Uses @ModelAttribute for cleaner form binding
     */
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @ModelAttribute CreatePostForm form,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {
        
        Long userId = extractUserId(userDetails);
        
        // Convert form to request, but clear poll and group (cannot be updated)
        CreatePostRequest request = new CreatePostRequest(
            form.getContent(),
            form.getTags(),
            form.getMentions(),
            form.getMediaFiles(),
            null, // Poll cannot be updated
            null, // Group cannot be changed
            form.getVisibility()
        );
        
        PostResponse response = postService.updatePost(postId, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a post
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        postService.deletePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
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
