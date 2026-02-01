package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoomRepository extends JpaRepository<UserRoom,Long> {

    List<UserRoom> findByUserId(Long id);
    List<UserRoom> findByChatRoomId(Long id);
}
