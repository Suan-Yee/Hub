package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupReport {
    private List<String> groupList;
    private List<Integer> postCount;
    private List<Integer> memberCount;
}
