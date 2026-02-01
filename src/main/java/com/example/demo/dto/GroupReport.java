package com.example.demo.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupReport(
        List<String> groupList,
        List<Integer> postCount,
        List<Integer> memberCount
) {
    public List<String> getGroupList() {
        return groupList;
    }

    public List<Integer> getPostCount() {
        return postCount;
    }

    public List<Integer> getMemberCount() {
        return memberCount;
    }
}
