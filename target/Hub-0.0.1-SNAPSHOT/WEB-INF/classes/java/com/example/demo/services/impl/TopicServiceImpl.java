package com.example.demo.services.impl;

import com.example.demo.dto.TopicDto;
import com.example.demo.dtoMapper.TopicDtoMapper;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostTopic;
import com.example.demo.entity.Topic;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.PostTopicRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.services.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final PostTopicRepository postTopicRepository;

   /* @Override
    public List<TopicDto> findAllTopic() {
        List<Topic> topics = topicRepository.findAll();
        return topics.stream().map(topic -> {
            TopicDto topicDto = new TopicDto(topic);
            int totalPostByTopic = 0;
            if (topic.getPostTopics() != null) {
                totalPostByTopic = (int) topic.getPostTopics().stream()
                        .filter(postTopic -> !postTopic.getPost().isStatus())
                        .count();
            }
            topicDto.setTotalPost(totalPostByTopic);
            return topicDto;
        }).collect(Collectors.toList());
    }*/

    @Override
    public List<TopicDto> findTrendingTopic() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Topic> topics = topicRepository.findTop5TopicsByPostCount(pageable);
        return topics.stream().map(topic -> {
            TopicDto topicDto = new TopicDto(topic);
            int totalPostByTopic = 0;
            if (topic.getPostTopics() != null) {
                totalPostByTopic = (int) topic.getPostTopics().stream()
                        .filter(postTopic -> !postTopic.getPost().isStatus())
                        .count();
            }
            topicDto.setTotalPost(totalPostByTopic);
            log.info("Total Post in Topic {}",topicDto.getTotalPost());
            return topicDto;
        }).collect(Collectors.toList());
    }

    @Override
    public TopicDto findByTopicName(String name) {
       Topic topic = topicRepository.findByNameContaining(name).orElse(null);
        return new TopicDto(topic);
    }
    @Override
    public Topic createTopic(String text) {
        Topic newTopic=new Topic();
        newTopic.setName(text);
        return topicRepository.save(newTopic);
    }
    @Override
    public Topic findOrCreateTopicByName(String name) {
        Optional<Topic> existingTopic = topicRepository.findByNameContaining(name);
        if (existingTopic.isPresent()) {
            return existingTopic.get();
        } else {
            Topic newTopic = new Topic();
            newTopic.setName(name);
            return topicRepository.save(newTopic);
        }
    }
    @Override
    public void saveTopicAndPost(Post post, List<String> topicNames) {

        for(String name : topicNames){
            PostTopic postTopic = new PostTopic();
            Topic topic = findOrCreateTopicByName(name);
            PostTopic existOrNot = postTopicRepository.findByTopicIdAndPostId(post.getId(), topic.getId()).orElse(null);
            if(existOrNot == null){
                postTopic.setTopic(topic);
                postTopic.setPost(post);
                postTopicRepository.save(postTopic);
            }

        }
    }
    @Transactional
    @Override
    public void deleteTopicFromPost(List<String> topicNames,Long postId){
        for (String topic : topicNames){
            postTopicRepository.deleteTopicFromPost(topic,postId);
        }
    }

    @Override
    public void extractTopicList(Post post,String text) {
       List<String> topicList = extractTopic(text);
       if(topicList != null && !topicList.isEmpty()){
           saveTopicAndPost(post,topicList);
       }
    }

    @Override
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return topicRepository.findById(id);
    }

    @Override
    public Topic update(Topic topic, String name) {
        topic.setName(name);
        return topicRepository.save(topic);
    }

    @Override
    public List<String> extractTopic(String text) {
        List<String> hashtags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            hashtags.add(matcher.group(1));
        }
        return hashtags;
    }
    @Override
     public String extractTopicWithoutHash(String text) {
        StringBuilder topicsBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("#(\\w+)|\\b(\\w+)\\b");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            if (matcher.group(2) != null) {
                topicsBuilder.append(matcher.group(2)).append(" ");
            }
        }
        return topicsBuilder.toString().trim();
    }
}
