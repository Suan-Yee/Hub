package com.example.demo.services.impl;

import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.services.CommentService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;


@Service
@RequiredArgsConstructor @Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    @Override
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<CommentDto> fetchAllComment(Long postId) {
        List<Comment> commentList = commentRepository.fetchCommentByPostIdWithoutParent(postId);
        if(!commentList.isEmpty()){
            return commentList.stream().map(comment -> {
                CommentDto commentDto = new CommentDto(comment);
                commentDto.setChildComment(countByTopLevelComment(postId,comment.getId()));
                commentDto.setTime(formatTimeAgo(comment.getCreatedAt()));
                commentDto.setOwner(authenticatedUser(commentDto));
                return commentDto;
            }).collect(Collectors.toList());
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public Long countCommentByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    @Override
    public List<CommentDto> findByParentId(Long parentId) {
        List<Comment> commentList = commentRepository.findByParentId(parentId);

        if(commentList.isEmpty()){
            return Collections.emptyList();
        }else {
            return commentList.stream().map(comment -> {
                CommentDto commentDto = new CommentDto(comment);
                commentDto.setTime(formatTimeAgo(comment.getCreatedAt()));
                commentDto.setOwner(authenticatedUser(commentDto));
                return commentDto;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public CommentDto createCommentDto(Comment comment, User user) {
        return new CommentDto(comment);
    }

    @Override
    public Comment createComment(User user, Post post, CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setText(commentDto.getText());

        if (commentDto.getParentCommentId() != null) {
            Comment parentComment = findById(commentDto.getParentCommentId());
            if (parentComment == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
            comment.setParentComment(parentComment);

            if (parentComment.getParentComment() == null) {
                comment.setRootComment(null);
            } else {
                Comment rootComment = findRootComment(parentComment);
                comment.setRootComment(rootComment);
            }
        }

        return saveComment(comment);
    }

    private Comment findRootComment(Comment comment) {

        while (comment.getParentComment() != null) {
            comment = comment.getParentComment();
        }
        return comment;
    }
    private boolean authenticatedUser(CommentDto commentDto){
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByStaffId(staffId);
        return commentDto.getUserId().equals(user.getId());
    }

    @Override
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    @Override
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comment with ID " + commentId + " does not exist.");
        }
        try {
            commentRepository.deleteById(commentId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete comment due to an error.", e);
        }
    }

    @Override
    public Long countByTopLevelComment(Long postId,Long commentId) {
        return commentRepository.countByTopLevelComment(postId,commentId);
    }

    @Override
    public void editComment(Long commentId,String text) {
        Comment comment = findById(commentId);
        comment.setIsEdited(true);
        comment.setText(text);
        commentRepository.save(comment);
    }
}
