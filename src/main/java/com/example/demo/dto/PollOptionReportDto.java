package com.example.demo.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PollOptionReportDto(
        String name,
        List<UserDto> users,
        long voteCount
) {
    public String getName() {
        return name;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public long getVoteCount() {
        return voteCount;
    }
}
