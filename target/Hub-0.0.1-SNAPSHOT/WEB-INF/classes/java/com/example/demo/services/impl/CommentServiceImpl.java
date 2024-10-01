package com.example.demo.services.impl;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.MentionDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Mention;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentLikeRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.MentionRepository;
import com.example.demo.services.CommentLikeService;
import com.example.demo.services.CommentService;
import com.example.demo.services.NotificationService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;


@Service
@RequiredArgsConstructor @Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final CommentLikeRepository commentLikeRepository;
    private final MentionRepository mentionRepository;

    @Override
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Page<CommentDto> fetchAllComment(int page, int size , Long postId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage = commentRepository.fetchCommentByPostIdWithoutParent(postId, pageable);

        if (commentPage.hasContent()) {
            List<CommentDto> commentDtoList = commentPage.getContent().stream().map(comment -> {
                CommentDto commentDto = new CommentDto(comment);
                commentDto.setChildComment(countByTopLevelComment(postId, comment.getId()));
                commentDto.setTime(formatTimeAgo(comment.getCreatedAt()));
                commentDto.setOwner(authenticatedUser(commentDto));
                commentDto.setCommentLiked(isCommentLikedByUser(comment));
                commentDto.setTotalLike(getTotalLikeForComment(comment));
                commentDto.setMentionUserList(findMentionUserByCommentId(comment.getId()));
                return commentDto;
            }).collect(Collectors.toList());
            return new PageImpl<>(commentDtoList, pageable, commentPage.getTotalElements());
        } else {
            return Page.empty(pageable);
        }
    }
    private Long getTotalLikeForComment(Comment comment){
        return commentRepository.getTotalLike(comment.getId());
    }

    private boolean isCommentLikedByUser(Comment comment) {
        String staffId = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userService.findByStaffId(staffId).getId();
        return comment.getLikes().stream()
                .anyMatch(like -> like.getUser().getId().equals(userId) && like.isLikeStatus());
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
                commentDto.setTotalLike((long) comment.getLikes().size());
                commentDto.setCommentLiked(isCommentLikedByUser(comment));
                commentDto.setMentionUserList(findMentionUserByCommentId(comment.getId()));
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

        log.info("CommentDTO {}",commentDto);

        if (commentDto.getMention() == null || commentDto.getMention().isEmpty()) {
            comment.setText(commentDto.getText());
        } else {
            String clearedText = clearUserMention(commentDto.getText(), commentDto.getMention());
            comment.setText(clearedText);
        }

        if (commentDto.getParentCommentId() != null) {
            Comment parentComment = findById(commentDto.getParentCommentId());
            if (parentComment == null) {
                throw new IllegalArgumentException("Parent comment not found");
            }
            if(!Objects.equals(parentComment.getUser().getId(), user.getId())){
                notificationService.notifyUser(parentComment.getUser().getStaffId(),comment.getUser().getName() + " reply to your comment");
            }
            comment.setParentComment(parentComment);

            if (parentComment.getParentComment() == null) {
                comment.setRootComment(parentComment);
            } else {
                Comment rootComment = findRootComment(parentComment);
                comment.setRootComment(rootComment);
            }
        }

        Comment savedComment = saveComment(comment);

        if(savedComment.getParentComment() != null){
            Comment parentComment = findById(savedComment.getParentComment().getId());
            if(!Objects.equals(parentComment.getUser().getId(), user.getId())){
                notificationService.createNotificationForReplyComment(comment.getPost(),savedComment,true);
            }
        }

        if(!user.getStaffId().equals(post.getUser().getStaffId())){
            notificationService.notifyUser(post.getUser().getStaffId(),user.getName() + " comment on your post");
            notificationService.createNotificationForComment(post,savedComment,user,true);
        }

        return savedComment;
    }
    public String clearUserMention(String text, List<String> staffId) {
        List<String> validStaffIds = new ArrayList<>();

        for (String each : staffId) {
            User user = userService.findByStaffId(each);
            if (user != null) {
                String userName = user.getName();
                String mentionPattern = "@" + userName;
                if (text.contains(mentionPattern)) {
                    validStaffIds.add(each);
                }
                text = text.replace(mentionPattern, "").trim();
            }
        }
        staffId.clear();
        staffId.addAll(validStaffIds);

        return text;
    }
    @Override
    public List<MentionDto> saveMentionUser(List<String> staffId, Comment comment) {
        List<MentionDto> mentionDtoList = new ArrayList<>();
        for (String staff : staffId) {
            Mention mention = new Mention();
            User user = userService.findByStaffId(staff);
            mention.setUser(user);
            mention.setComment(comment);
            Mention savedMention = mentionRepository.save(mention);
            MentionDto mentionDto = changeToMentionDto(savedMention);
            mentionDto.setUserStaffId(staff);
            mentionDtoList.add(mentionDto);
        }
        return mentionDtoList;
    }

    @Override
    public List<MentionDto> findMentionUserByCommentId(Long commentId) {
        Comment comment = findById(commentId);
        return comment.getMentions().stream().map(MentionDto::new).toList();
    }

    @Override
    public List<MentionDto> saveMentionNotificationAndUser(CommentDto commentDto,Comment comment) {
        List<MentionDto> mentionDtoList = saveMentionUser(commentDto.getMention(),comment);
        for(String staff : commentDto.getMention()){
            User user = userService.findByStaffId(staff);
            if(Objects.equals(comment.getPost().getUser().getId(),comment.getUser().getId())){
                String text = comment.getUser().getName() + " mention you!";
                notificationService.createNotificationForMention(text,comment.getPost(),comment,user,true);
                notificationService.notifyUser(staff,text);
            }else{
                String text = comment.getUser().getName() + " mention you in the " + comment.getPost().getUser().getName() + " post";
                notificationService.createNotificationForMention(text,comment.getPost(),comment,user,true);
                notificationService.notifyUser(staff,text);
            }
        }
        return mentionDtoList;
    }

    private MentionDto changeToMentionDto(Mention mention){
        MentionDto mentionDto = new MentionDto(mention);

        return mentionDto;
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
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment with ID " + commentId + " does not exist."));
        deleteCommentAndChildren(comment);
    }

    private void deleteCommentAndChildren(Comment comment) {
        for (Comment childComment : comment.getReplies()) {
            deleteCommentAndChildren(childComment);
        }
//        notificationService.deleteNotificationByCommentId(comment.getId());
        commentLikeRepository.deleteByCommentId(comment.getId());
        commentRepository.delete(comment);
    }


    @Override
    public Long countByTopLevelComment(Long postId,Long commentId) {
        return commentRepository.countByTopLevelComment(postId,commentId);
    }

    @Override
    public CommentDto editComment(Long commentId, String text, List<String> userMention) {
        Comment comment = findById(commentId);
        comment.setIsEdited(true);
        comment.setText(text);
        List<User> newMentionedUsers = userMention.stream().map(userService::findByStaffId).toList();
        List<Mention> mentionsToRemove = comment.getMentions().stream().filter(mention -> !newMentionedUsers.contains(mention.getUser())).toList();

        mentionsToRemove.forEach(mention -> comment.getMentions().remove(mention));

        for (User user : newMentionedUsers) {
            boolean alreadyMentioned = comment.getMentions().stream()
                    .anyMatch(mention -> mention.getUser().equals(user));
            if (!alreadyMentioned) {
                Mention newMention = new Mention();
                newMention.setUser(user);
                newMention.setComment(comment);
                comment.getMentions().add(newMention);
            }
        }
        Comment newComment = commentRepository.save(comment);
        CommentDto commentDto = new CommentDto(newComment);
        commentDto.setChildComment(countByTopLevelComment(comment.getPost().getId(), comment.getId()));
        commentDto.setTime(formatTimeAgo(comment.getCreatedAt()));
        commentDto.setOwner(authenticatedUser(commentDto));
        commentDto.setCommentLiked(isCommentLikedByUser(comment));
        commentDto.setTotalLike(getTotalLikeForComment(comment));
        commentDto.setMentionUserList(findMentionUserByCommentId(comment.getId()));
        return commentDto;
    }
}
