package com.example.demo.dtoMapper;

import com.example.demo.dto.GroupReport;
import com.example.demo.entity.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupReportMapper {
    public GroupReport mapTogroupReport(List<Group> groups) {
        List<String> groupNames = groups.stream()
                .map(Group::getName)
                .collect(Collectors.toList());

        List<Integer> postCounts = groups.stream()
                .map(group->group.getPosts().size())
                .collect(Collectors.toList());
        List<Integer> memberCounts = groups.stream()
                .map(group->group.getUserHasGroups().size())
                .collect(Collectors.toList());
        return GroupReport.builder()
                .groupList(groupNames)
                .postCount(postCounts)
                .memberCount(memberCounts)
                .build();
    }
}
