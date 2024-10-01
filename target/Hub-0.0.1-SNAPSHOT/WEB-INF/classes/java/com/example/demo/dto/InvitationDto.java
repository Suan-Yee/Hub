package com.example.demo.dto;

import com.example.demo.entity.UserInvitation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class InvitationDto {

    private String groupName;
    private String requestUserName;
    private Long groupId;
    private Long requestUserStaffId;
    private String userProfile;
}
