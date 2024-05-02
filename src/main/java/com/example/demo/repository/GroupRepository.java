package com.example.demo.repository;


import com.example.demo.entity.Group;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group,Long>, JpaSpecificationExecutor<Group> {

    @Query("select g FROM  Group g where g.deleted = FALSE ")
    List<Group> findAllGroup();

}