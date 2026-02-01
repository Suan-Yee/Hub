package com.example.demo.dtoMapper;

import com.example.demo.dto.GroupPostPopulation;
import com.example.demo.entity.Group;
import com.example.demo.infrastructure.persistence.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupPostPopulationMapper {
    private final GroupRepository groupRepository;

    public GroupPostPopulation mapToGroupPostPopulation(Group group, int year){
        List<Object[]> postCounts = groupRepository.findPostCountByGroupAndYear(group.getId(),year);

        List<Integer> monthlyPostCounts = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            monthlyPostCounts.add(0);
        }
        postCounts.forEach(record -> {
            int month = (int) record[0] - 1; // Month is 1-based in SQL but 0-based in Java
            int count = ((Number) record[1]).intValue();
            monthlyPostCounts.set(month, count);
        });

        return GroupPostPopulation.builder()
                .id(group.getId())
                .groupName(group.getName())
                .postPopulation(monthlyPostCounts)
                .build();
    }
}
