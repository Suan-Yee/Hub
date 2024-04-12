package com.example.demo.services.impl;

import com.example.demo.dto.TopicDto;
import com.example.demo.dtoMapper.TopicDtoMapper;
import com.example.demo.entity.Topic;
import com.example.demo.repository.TopicRepository;
import com.example.demo.services.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    @Override
    public List<TopicDto> findAllTopic() {

        List<Topic> topics  = topicRepository.findAll();
        return topics.stream().map(TopicDtoMapper::fromTopic).collect(Collectors.toList());

    }

    @Override
    public TopicDto findByTopicName(String name) {
       Topic topic = topicRepository.findByName(name).orElse(null);
        return null;
    }
}
