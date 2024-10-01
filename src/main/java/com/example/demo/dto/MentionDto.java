package com.example.demo.dto;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Mention;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class MentionDto {

    private Long userId;
    private String userName;
    private Long commentId;
    private Long postId;
    private String userStaffId;

    public MentionDto(Mention mention){
        if (mention.getUser() != null) {
            this.userId = mention.getUser().getId();
            this.userName = mention.getUser().getName();
            this.userStaffId = mention.getUser().getStaffId();
        }
        if (mention.getComment() != null) {
            this.commentId = mention.getComment().getId();
        }
        if (mention.getPost() != null) {
            this.postId = mention.getPost().getId();
        }
    }
}
