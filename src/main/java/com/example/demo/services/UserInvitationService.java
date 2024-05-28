package com.example.demo.services;

import com.example.demo.dto.UserInvitationDto;
import com.example.demo.entity.UserInvitation;

import java.util.List;

public interface UserInvitationService {

    void save(Long id, UserInvitationDto userInvitationDto);

    List<UserInvitation> findLoginUserInvitation(Long id);

     void findById(Long id);

     void acceptedInvitation(Long id, Long communityId);

}
