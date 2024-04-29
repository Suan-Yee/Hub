package com.example.demo.dto;

import com.example.demo.entity.Comment;
import lombok.*;

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
    private Long rootCommentId;
    private Long childComment;
    private String time;
    private boolean isEdited;
    private boolean isOwner;

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
        }

        if (comment.getRootComment() != null) {
            this.rootCommentId = comment.getRootComment().getId();
        }
    }

}
