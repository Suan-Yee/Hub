package com.example.demo.application.usecase;

import com.example.demo.dto.TopicDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicService {

   /* List<TopicDto> findAllTopic();*/
    List<TopicDto> findTrendingTopic();
    TopicDto findByTopicName(String name);
    Topic createTopic(String text);
    void deleteTopic(Long id);
    Optional<Topic> findById(Long id);
    Topic update(Topic topic, String name);
    List<String> extractTopic(String text);
    Topic findOrCreateTopicByName(String name);
    void saveTopicAndPost(Post post, List<String> topicNames);
    void extractTopicList(Post post,String text);
    String extractTopicWithoutHash(String text);
    void deleteTopicFromPost(List<String> topicNames,Long postId);
}
