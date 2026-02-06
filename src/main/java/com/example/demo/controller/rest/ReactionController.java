package com.example.demo.controller.rest;

import com.example.demo.enumeration.ReactionType;
import com.example.demo.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {

    private final ReactionService reactionService;

    /**
     * Add or update reaction to a post
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<Map<String, String>> addReactionToPost(
            @PathVariable Long postId,
            @RequestParam ReactionType reactionType,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        reactionService.addReactionToPost(postId, userId, reactionType);
        return ResponseEntity.ok(Map.of("message", "Reaction added successfully"));
    }

    /**
     * Remove reaction from a post
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Map<String, String>> removeReactionFromPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        reactionService.removeReactionFromPost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Reaction removed successfully"));
    }

    /**
     * Add or update reaction to a comment
     */
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, String>> addReactionToComment(
            @PathVariable Long commentId,
            @RequestParam ReactionType reactionType,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        reactionService.addReactionToComment(commentId, userId, reactionType);
        return ResponseEntity.ok(Map.of("message", "Reaction added successfully"));
    }

    /**
     * Remove reaction from a comment
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, String>> removeReactionFromComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = extractUserId(userDetails);
        reactionService.removeReactionFromComment(commentId, userId);
        return ResponseEntity.ok(Map.of("message", "Reaction removed successfully"));
    }

    /**
     * Extract user ID from UserDetails
     */
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }
        // TODO: Implement based on your UserDetails implementation
        return 1L;
    }
}
