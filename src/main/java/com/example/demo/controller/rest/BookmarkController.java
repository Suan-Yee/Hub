package com.example.demo.controller.rest;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Slf4j
public class BookmarkController {

    private final BookmarkService bookmarkService;

//    /**
//     * Add a bookmark to a post
//     */
//    @PostMapping("/post/{postId}")
//    public ResponseEntity<Map<String, String>> addBookmark(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long userId = extractUserId(userDetails);
//        bookmarkService.addBookmark(postId, userId);
//        return ResponseEntity.ok(Map.of("message", "Post bookmarked successfully"));
//    }
//
//    /**
//     * Remove a bookmark from a post
//     */
//    @DeleteMapping("/post/{postId}")
//    public ResponseEntity<Map<String, String>> removeBookmark(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long userId = extractUserId(userDetails);
//        bookmarkService.removeBookmark(postId, userId);
//        return ResponseEntity.ok(Map.of("message", "Bookmark removed successfully"));
//    }
//
//    /**
//     * Get all bookmarked posts for the current user
//     */
//    @GetMapping
//    public ResponseEntity<Page<PostResponse>> getUserBookmarks(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long userId = extractUserId(userDetails);
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<PostResponse> bookmarks = bookmarkService.getUserBookmarks(userId, userId, pageable);
//        return ResponseEntity.ok(bookmarks);
//    }
//
//    /**
//     * Get bookmarked posts for a specific user
//     */
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<Page<PostResponse>> getUserBookmarksByUserId(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long currentUserId = extractUserId(userDetails);
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<PostResponse> bookmarks = bookmarkService.getUserBookmarks(userId, currentUserId, pageable);
//        return ResponseEntity.ok(bookmarks);
//    }
//
//    /**
//     * Check if a post is bookmarked
//     */
//    @GetMapping("/post/{postId}/check")
//    public ResponseEntity<Map<String, Boolean>> checkBookmark(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        Long userId = extractUserId(userDetails);
//        boolean isBookmarked = bookmarkService.isBookmarked(postId, userId);
//        return ResponseEntity.ok(Map.of("bookmarked", isBookmarked));
//    }
//
//    /**
//     * Extract user ID from UserDetails
//     */
//    private Long extractUserId(UserDetails userDetails) {
//        if (userDetails == null) {
//            throw new RuntimeException("User not authenticated");
//        }
//        // TODO: Implement based on your UserDetails implementation
//        return 1L;
//    }
}
