package com.example.demo.dtoMapper;

import com.example.demo.dto.TopicDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;

public class TopicDtoMapper {

    public static TopicDto fromTopic(Topic topic){
        TopicDto topicDto = new TopicDto();
        BeanUtils.copyProperties(topic,topicDto);
        return topicDto;
    }

    public static Topic toTopic(TopicDto topicDto){
        Topic topic = new Topic();
        BeanUtils.copyProperties(topicDto,topic);
        return topic;
    }

}
