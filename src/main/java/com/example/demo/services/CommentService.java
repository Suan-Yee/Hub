package com.example.demo.services;

import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;

import java.util.List;

public interface CommentService {

    Comment saveComment(Comment comment);
    List<CommentDto> fetchAllComment(Long postId);
    Long countCommentByPostId(Long postId);
    List<CommentDto> findByParentId(Long parentId);
    CommentDto createCommentDto(Comment comment, User user);
    Comment createComment(User user, Post post, CommentDto commentDto);
    Comment findById(Long commentId);
    void deleteComment(Long commentId);
    Long countByTopLevelComment(Long postId,Long commentId);
    void editComment(Long commentId,String text);

}
