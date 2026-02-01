package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.UserHasGroup;
import com.example.demo.entity.UserRequestGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRequestGroupRepository extends JpaRepository<UserRequestGroup,Long> {

    List<UserRequestGroup> findByUserId(Long id);

   Optional<UserRequestGroup> findById(Long id);



}
