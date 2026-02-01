package com.example.demo.application.usecase;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Mention;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    Comment saveComment(Comment comment);
    Page<CommentDto> fetchAllComment(int page, int size , Long postId);
    Long countCommentByPostId(Long postId);
    List<CommentDto> findByParentId(Long parentId);
    CommentDto createCommentDto(Comment comment, User user);
    Comment createComment(User user, Post post, CommentDto commentDto);
    Comment findById(Long commentId);
    void deleteComment(Long commentId);
    Long countByTopLevelComment(Long postId,Long commentId);
    CommentDto editComment(Long commentId, String text, List<String> userMention);
    List<MentionDto> saveMentionUser(List<String> staffId, Comment comment);
    List<MentionDto> findMentionUserByCommentId(Long commentId);
    List<MentionDto> saveMentionNotificationAndUser(CommentDto commentDto,Comment comment);
}
