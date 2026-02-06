package com.example.demo.service;

import com.example.demo.enumeration.ReactionType;

public interface ReactionService {
    
    /**
     * Add or update a reaction to a post
     * @param postId The post ID
     * @param userId The user ID
     * @param reactionType The reaction type
     */
    void addReactionToPost(Long postId, Long userId, ReactionType reactionType);
    
    /**
     * Remove a reaction from a post
     * @param postId The post ID
     * @param userId The user ID
     */
    void removeReactionFromPost(Long postId, Long userId);
    
    /**
     * Add or update a reaction to a comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @param reactionType The reaction type
     */
    void addReactionToComment(Long commentId, Long userId, ReactionType reactionType);
    
    /**
     * Remove a reaction from a comment
     * @param commentId The comment ID
     * @param userId The user ID
     */
    void removeReactionFromComment(Long commentId, Long userId);
}
