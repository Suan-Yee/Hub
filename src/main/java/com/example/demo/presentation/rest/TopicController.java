package com.example.demo.presentation.rest;

import com.example.demo.dto.TopicDto;
import com.example.demo.entity.Topic;
import com.example.demo.application.usecase.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor @Slf4j
@RequestMapping("/topic")
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/all")
    public ResponseEntity<?> fetchAllTopic(){
        List<TopicDto> topics = topicService.findTrendingTopic();
        log.info("Topics {} ",topics);
        return new ResponseEntity<>(topics, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Topic> createTopic (@RequestBody TopicDto topicDto){
        Topic saveTopic = topicService.createTopic(topicDto.getName());
        return new ResponseEntity<>(saveTopic, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<Topic> updateTopic (@RequestBody TopicDto topicDto){
        Optional<Topic> getTopic = topicService.findById(topicDto.getId());
        if (getTopic.isPresent()){
            Topic updateTopic = topicService.update(getTopic.get(), topicDto.getName());
            return ResponseEntity.ok(updateTopic);
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTopic (@RequestBody TopicDto topicDto){
        Optional<Topic> existTopic = topicService.findById(topicDto.getId());
        if (existTopic.isPresent()) {
            topicService.deleteTopic(topicDto.getId());
            return ResponseEntity.ok("Topic with ID "+ topicDto.getId() + " delete successfully.");
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}
