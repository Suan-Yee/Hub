package com.example.demo.dtoMapper;

import com.example.demo.dto.TopicDto;
import com.example.demo.entity.Topic;

public class TopicDtoMapper {

    public static TopicDto fromTopic(Topic topic) {
        return new TopicDto(topic);
    }

    public static Topic toTopic(TopicDto topicDto) {
        Topic topic = new Topic();
        topic.setId(topicDto.getId());
        topic.setName(topicDto.getName());
        return topic;
    }

}
