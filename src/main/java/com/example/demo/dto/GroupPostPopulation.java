package com.example.demo.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupPostPopulation(
        Long id,
        String groupName,
        List<Integer> postPopulation
) {
    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Integer> getPostPopulation() {
        return postPopulation;
    }
}
