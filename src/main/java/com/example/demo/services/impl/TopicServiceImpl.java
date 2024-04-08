package com.example.demo.services.impl;

import com.example.demo.entity.Topic;
import com.example.demo.repository.TopicRepository;
import com.example.demo.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    public List<Topic> findAllTopic() {
        return topicRepository.findAll();
    }

    @Override
    public Topic findByTopicName(String name) {
        return null;
    }
}
