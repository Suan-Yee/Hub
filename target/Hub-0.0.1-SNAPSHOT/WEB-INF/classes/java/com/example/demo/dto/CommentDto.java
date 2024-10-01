package com.example.demo.dto;

import com.example.demo.entity.Comment;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    private Long userId;
    private Long postId;
    private String text;
    private String userName;
    private String userImage;
    private Long parentCommentId;
    private String parentUserName;
    private Long rootCommentId;
    private Long childComment;
    private String time;
    private boolean isEdited;
    private boolean isOwner;
    private boolean isCommentLiked;
    private Long totalLike;
    private List<String> mention;
    private List<MentionDto> mentionUserList;
    private boolean replyToIsOwnComment = false;

    public CommentDto (Comment comment){
        this.id = comment.getId();
        this.userId = comment.getUser().getId();
        this.postId = comment.getPost().getId();
        this.text = comment.getText();
        this.userName = comment.getUser().getName();
        this.userImage = comment.getUser().getPhoto();
        this.isEdited = comment.isIsEdited();

        if (comment.getParentComment() != null) {
            this.parentCommentId = comment.getParentComment().getId();
            this.parentUserName = comment.getParentComment().getUser().getName();
            if(Objects.equals(comment.getParentComment().getUser().getStaffId(),comment.getUser().getStaffId())){
                replyToIsOwnComment = true;
            }
        }

        if (comment.getRootComment() != null) {
            this.rootCommentId = comment.getRootComment().getId();
        }
    }

}
