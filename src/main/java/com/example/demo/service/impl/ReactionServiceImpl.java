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

    @Override
    public void addReactionToPost(Long postId, Long userId, ReactionType reactionType) {

    }

    @Override
    public void removeReactionFromPost(Long postId, Long userId) {

    }

    @Override
    public void addReactionToComment(Long commentId, Long userId, ReactionType reactionType) {

    }

    @Override
    public void removeReactionFromComment(Long commentId, Long userId) {

    }
}
