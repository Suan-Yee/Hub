package com.example.demo.service.impl;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enumeration.ReactionType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public void addReactionToPost(Long postId, Long userId, ReactionType reactionType) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));

        // Check if user already reacted
        Optional<Reaction> existingReaction = reactionRepository
            .findByUserIdAndTargetTypeAndTargetId(userId, "post", postId);

        if (existingReaction.isPresent()) {
            // Update existing reaction
            Reaction reaction = existingReaction.get();
            String oldReactionType = reaction.getReactionType();
            reaction.setReactionType(reactionType.name().toLowerCase());
            reactionRepository.save(reaction);
            log.info("Updated reaction on post {} from {} to {} by user {}", 
                postId, oldReactionType, reactionType, userId);
        } else {
            // Create new reaction
            Reaction reaction = Reaction.builder()
                .user(user)
                .targetType("post")
                .targetId(postId)
                .reactionType(reactionType.name().toLowerCase())
                .build();
            reactionRepository.save(reaction);
            
            // Increment likes count on post
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
            
            log.info("Added reaction {} to post {} by user {}", reactionType, postId, userId);
        }
    }

    @Override
    public void removeReactionFromPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ApiException("Post not found"));

        Optional<Reaction> reaction = reactionRepository
            .findByUserIdAndTargetTypeAndTargetId(userId, "post", postId);

        if (reaction.isPresent()) {
            reactionRepository.delete(reaction.get());
            
            // Decrement likes count on post
            if (post.getLikesCount() > 0) {
                post.setLikesCount(post.getLikesCount() - 1);
                postRepository.save(post);
            }
            
            log.info("Removed reaction from post {} by user {}", postId, userId);
        }
    }

    @Override
    public void addReactionToComment(Long commentId, Long userId, ReactionType reactionType) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ApiException("Comment not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException("User not found"));

        // Check if user already reacted
        Optional<Reaction> existingReaction = reactionRepository
            .findByUserIdAndTargetTypeAndTargetId(userId, "comment", commentId);

        if (existingReaction.isPresent()) {
            // Update existing reaction
            Reaction reaction = existingReaction.get();
            reaction.setReactionType(reactionType.name().toLowerCase());
            reactionRepository.save(reaction);
            log.info("Updated reaction on comment {} to {} by user {}", 
                commentId, reactionType, userId);
        } else {
            // Create new reaction
            Reaction reaction = Reaction.builder()
                .user(user)
                .targetType("comment")
                .targetId(commentId)
                .reactionType(reactionType.name().toLowerCase())
                .build();
            reactionRepository.save(reaction);
            log.info("Added reaction {} to comment {} by user {}", reactionType, commentId, userId);
        }
    }

    @Override
    public void removeReactionFromComment(Long commentId, Long userId) {
        commentRepository.findById(commentId)
            .orElseThrow(() -> new ApiException("Comment not found"));

        Optional<Reaction> reaction = reactionRepository
            .findByUserIdAndTargetTypeAndTargetId(userId, "comment", commentId);

        if (reaction.isPresent()) {
            reactionRepository.delete(reaction.get());
            log.info("Removed reaction from comment {} by user {}", commentId, userId);
        }
    }
}
