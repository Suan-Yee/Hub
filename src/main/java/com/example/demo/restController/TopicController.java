package com.example.demo.restController;

import com.example.demo.dto.TopicDto;
import com.example.demo.services.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor @Slf4j
@RequestMapping("/topic")
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/all")
    public ResponseEntity<?> fetchAllTopic(){
        List<TopicDto> topics = topicService.findAllTopic();
        log.info("Topics {} ",topics);
        return new ResponseEntity<>(topics, HttpStatus.OK);
    }
}
