package com.example.demo.repository;

import com.example.demo.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation,Long> {

    List<UserInvitation> findByRecipientIdAndIsInvited(Long id, boolean isInvited);

    UserInvitation findByRecipientIdAndGroupId(Long userId, Long id);

    @Query("SELECT i FROM UserInvitation i WHERE i.requestId = :userId and i.group.id =:groupId")
    Optional<UserInvitation> getInfoForUserRequest(@Param("userId")Long userId,@Param("groupId")Long groupId);

    @Query("SELECT i FROM UserInvitation i WHERE i.recipientId = :recipientId AND i.group.id = :groupId AND i.requestId IS NOT NULL ")
    List<UserInvitation> getAllUserRequest(@Param("recipientId")Long userId,@Param("groupId")Long groupId);

    @Transactional
    @Modifying
    @Query("DELETE  FROM UserInvitation i WHERE i.requestId = :recipientId AND i.group.id = :groupId")
    void removeUserRequest(@Param("recipientId")Long userId,@Param("groupId")Long groupId);


}
