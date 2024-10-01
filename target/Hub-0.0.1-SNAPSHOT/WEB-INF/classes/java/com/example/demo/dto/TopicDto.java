package com.example.demo.dto;

import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class TopicDto {

    private Long id;
    private String name;
    private int totalPost;

    public TopicDto(Topic topic){
        this.id = topic.getId();
        this.name = topic.getName();
    }
}
