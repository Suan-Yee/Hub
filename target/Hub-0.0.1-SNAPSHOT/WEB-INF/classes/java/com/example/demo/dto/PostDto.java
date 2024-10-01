package com.example.demo.dto;

import com.example.demo.entity.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {

    private Long id;  //
    private boolean status;
    private ContentDto content;//
    private TopicDto topic;//
    private UserDto user;//
    private int likes;
    private boolean isLikedByCurrentUser;
    private boolean isBookMark;
    private String photo;//
    private String time;//
    private Long commentCount;
    private String groupName;
    private boolean owner;
    private boolean isAdmin;
    private List<MentionDto> mentionUserList;
    private List<String> topicList;

    public PostDto(Post post) {
        this.id = post.getId();
        this.content = new ContentDto(post.getContent());
        if(post.getTopic() != null){
            this.topic = new TopicDto(post.getTopic());
        }
        this.user = new UserDto(post.getUser());
        this.photo = post.getUser().getPhoto();
        this.time = formatTimeAgo(post.getCreatedAt());
        if(post.getGroup() != null){
            this.groupName = post.getGroup().getName();
        }
    }
}
