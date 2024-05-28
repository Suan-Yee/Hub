package com.example.demo.repository;

import com.example.demo.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInvitationRepository extends JpaRepository<UserInvitation,Long> {

    List<UserInvitation> findByRecipientIdAndIsInvited(Long id, boolean isInvited);

    UserInvitation findByRecipientIdAndGroupId(Long userId, Long id);

}
