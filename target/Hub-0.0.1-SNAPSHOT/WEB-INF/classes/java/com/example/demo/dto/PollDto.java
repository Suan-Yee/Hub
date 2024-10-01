package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@Builder
public class PollDto {
    private Long id;
    private String question;
    private Map<Long,String> answers;
    private Long pollCount;
    private List<Integer> answersWeight;
    private int selectedAnswer;
    private String expiredDate;
    private UserDto user;
    private Boolean isLoginUserPoll;
    private long groupId;
}
