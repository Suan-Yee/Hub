package com.example.demo.dto;

import com.example.demo.entity.Content;
import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {

    private Long id;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ContentDto content;
    private TopicDto topic;
    private UserDto user;


    public PostDto(Post post) {
        this.id = post.getId();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.content = new ContentDto(post.getContent());
        this.topic = new TopicDto(post.getTopic());
        this.user = new UserDto(post.getUser());
    }
}
