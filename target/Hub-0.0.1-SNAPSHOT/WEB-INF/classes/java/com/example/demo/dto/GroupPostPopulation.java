package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupPostPopulation {
    private Long id;
    private String groupName;
    private List<Integer> postPopulation;
}
