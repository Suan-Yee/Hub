package com.example.demo.presentation.websocket;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.application.usecase.CommentService;
import com.example.demo.application.usecase.PostService;
import com.example.demo.application.usecase.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;

import java.security.Principal;
import java.util.List;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Controller
@RequiredArgsConstructor
public class CommentWebSocketHandler {

    private final CommentService commentService;
    private final UserService userService;
    private final PostService postService;

    @MessageMapping("/comment/{postId}")
    @SendTo("/comment/messages/{postId}")
    public CommentDto send(@DestinationVariable Long postId, @Payload CommentDto commentDto, Principal principal) {
        User user = userService.findAuthenticatedUser(principal);
        Post post = postService.findById(postId);

        Comment savedComment = commentService.createComment(user, post, commentDto);
        CommentDto comment = commentService.createCommentDto(savedComment, user);
        if (commentDto.getMention() != null && !commentDto.getMention().isEmpty()) {
            List<MentionDto> mentionDtoList = commentService.saveMentionNotificationAndUser(commentDto, savedComment);
            comment = comment.withMentionUserList(mentionDtoList);
        }
        return comment.withTime(formatTimeAgo(savedComment.getCreatedAt())).withOwner(true);
    }
}
