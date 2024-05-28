package com.example.demo.repository;

import com.example.demo.entity.UserRequestGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRequestGroupRepository extends JpaRepository<UserRequestGroup,Long> {

    List<UserRequestGroup> findByUserId(Long id);

   Optional<UserRequestGroup> findById(Long id);

}
