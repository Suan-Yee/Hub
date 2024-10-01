package com.example.demo.repository;

import com.example.demo.entity.Group;
import com.example.demo.entity.GroupMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage,Long> {

    List<GroupMessage> findMessageByGroup(Group group, Pageable pageable);
}
