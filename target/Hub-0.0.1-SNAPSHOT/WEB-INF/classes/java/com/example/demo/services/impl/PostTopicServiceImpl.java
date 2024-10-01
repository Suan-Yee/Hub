package com.example.demo.services.impl;

import com.example.demo.repository.PostTopicRepository;
import com.example.demo.services.PostTopicService;
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
