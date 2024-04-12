package com.example.demo.services;

import com.example.demo.dto.TopicDto;
import com.example.demo.entity.Topic;

import java.util.List;

public interface TopicService {

    List<TopicDto> findAllTopic();
    TopicDto findByTopicName(String name);
}
