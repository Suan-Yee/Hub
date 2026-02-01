package com.example.demo.dto;

import com.example.demo.entity.Topic;

public record TopicDto(
        Long id,
        String name,
        int totalPost
) {
    public TopicDto(Topic topic) {
        this(topic.getId(), topic.getName(), 0);
    }

    public TopicDto withTotalPost(int value) {
        return new TopicDto(id, name, value);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalPost() {
        return totalPost;
    }
}
