package com.example.demo.services;

import com.example.demo.entity.Topic;

import java.util.List;

public interface TopicService {

    List<Topic> findAllTopic();
    Topic findByTopicName(String name);
}
