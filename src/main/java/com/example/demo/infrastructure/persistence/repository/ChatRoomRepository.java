package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.entity.ChatRoom;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

//    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.sender.id = :senderId AND cr.recipient.id = :recipientId) OR (cr.sender.id = :recipientId AND cr.recipient.id = :senderId)")
//    Optional<ChatRoom> findBySenderAndRecipientId(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);
//
//    Optional<ChatRoom> findByChatRoomId(String id);

    Optional<ChatRoom> findByUserAndRecipientId(User senderId, int recipientId);
}
