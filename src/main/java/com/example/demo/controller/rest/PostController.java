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

    @PostMapping
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

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<ApiResponse> getPostsByUserId(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
////            @AuthenticationPrincipal UserDetails userDetails,
//            HttpServletRequest request
//    ) {
//        Long userId = 1L;
//        Long currentUserId = 1L;
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<PostResponse> posts = postService.getPostsByUserId(userId, currentUserId, pageable);
//
//        ApiResponse apiResponse = ApiResponse.success(
//            request.getRequestURI(),
//            "User posts retrieved successfully",
//            Map.of(
//                "posts", posts.getContent(),
//                "pagination", Map.of(
//                    "page", posts.getNumber(),
//                    "size", posts.getSize(),
//                    "totalElements", posts.getTotalElements(),
//                    "totalPages", posts.getTotalPages()
//                )
//            )
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }
//
//    @GetMapping("/feed")
//    public ResponseEntity<ApiResponse> getPublicFeed(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            HttpServletRequest request
//    ) {
//        Long userId = 1L;
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<PostResponse> posts = postService.getPublicFeed(currentUserId, pageable);
//
//        ApiResponse apiResponse = ApiResponse.success(
//            request.getRequestURI(),
//            "Public feed retrieved successfully",
//            Map.of(
//                "posts", posts.getContent(),
//                "pagination", Map.of(
//                    "page", posts.getNumber(),
//                    "size", posts.getSize(),
//                    "totalElements", posts.getTotalElements(),
//                    "totalPages", posts.getTotalPages()
//                )
//            )
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }
//
//    @GetMapping("/group/{groupId}")
//    public ResponseEntity<ApiResponse> getPostsByGroupId(
//            @PathVariable Long groupId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails,
//            HttpServletRequest request
//    ) {
//        Long currentUserId = extractUserId(userDetails);
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<PostResponse> posts = postService.getPostsByGroupId(groupId, currentUserId, pageable);
//
//        ApiResponse apiResponse = ApiResponse.success(
//            request.getRequestURI(),
//            "Group posts retrieved successfully",
//            Map.of(
//                "posts", posts.getContent(),
//                "pagination", Map.of(
//                    "page", posts.getNumber(),
//                    "size", posts.getSize(),
//                    "totalElements", posts.getTotalElements(),
//                    "totalPages", posts.getTotalPages()
//                )
//            )
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }

//    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse> updatePost(
//            @PathVariable Long postId,
//            @Valid @ModelAttribute CreatePostRequest postRequest,
//            @AuthenticationPrincipal UserDetails userDetails,
//            HttpServletRequest request
//    ) throws IOException {
//
//        Long userId = extractUserId(userDetails);
//
//        PostResponse response = postService.updatePost(postId, postRequest, userId);
//
//        ApiResponse apiResponse = ApiResponse.success(
//                request.getRequestURI(),
//                "Post updated successfully",
//                Map.of("post", response)
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }
//    @DeleteMapping("/{postId}")
//    public ResponseEntity<ApiResponse> deletePost(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal UserDetails userDetails,
//            HttpServletRequest request
//    ) {
//        Long userId = extractUserId(userDetails);
//        postService.deletePost(postId, userId);
//
//        ApiResponse apiResponse = ApiResponse.success(
//            request.getRequestURI(),
//            "Post deleted successfully"
//        );
//
//        return ResponseEntity.ok(apiResponse);
//    }
//    private Long extractUserId(UserDetails userDetails) {
//        if (userDetails == null) {
//            return null;
//        }
//        // TODO: Implement based on your UserDetails implementation
//        // This might be something like:
//        // return ((CustomUserDetails) userDetails).getId();
//        // For now, returning a placeholder
//        return 1L;
//    }
}
