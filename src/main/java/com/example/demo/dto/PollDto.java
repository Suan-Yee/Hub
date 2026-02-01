package com.example.demo.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record PollDto(
        Long id,
        String question,
        Map<Long, String> answers,
        Long pollCount,
        List<Integer> answersWeight,
        int selectedAnswer,
        String expiredDate,
        UserDto user,
        Boolean isLoginUserPoll,
        long groupId
) {
    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public Long getPollCount() {
        return pollCount;
    }

    public List<Integer> getAnswersWeight() {
        return answersWeight;
    }

    public int getSelectedAnswer() {
        return selectedAnswer;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public UserDto getUser() {
        return user;
    }

    public Boolean getIsLoginUserPoll() {
        return isLoginUserPoll;
    }

    public long getGroupId() {
        return groupId;
    }
}
