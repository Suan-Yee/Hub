package com.example.demo.application.usecase.impl;

import com.example.demo.infrastructure.persistence.repository.PostTopicRepository;
import com.example.demo.application.usecase.PostTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostTopicServiceImpl implements PostTopicService {

    private final PostTopicRepository postTopicRepository;

    @Override
    public List<String> getAllTopicName(Long postId) {
        return postTopicRepository.topicName(postId);
    }
}
