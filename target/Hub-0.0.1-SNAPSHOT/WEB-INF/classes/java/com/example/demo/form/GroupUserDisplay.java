package com.example.demo.form;

import com.example.demo.entity.User;
import com.example.demo.entity.UserHasGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.example.demo.utils.TimeFormatter.formatTimeAgo;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class GroupUserDisplay {

    private Long userId;
    private String userName;
    private String userProfile;
    private String joinedDate;
    private String staffId;
    private int postCount;
    private Long groupId;
    private String status;
    private Long newAdmin;

    public GroupUserDisplay(UserHasGroup userHasGroup,int postCount){
        User user = userHasGroup.getUser();
        this.userId = user.getId();
        this.userName = user.getName();
        this.userProfile = user.getPhoto();
        this.staffId = user.getStaffId();
        LocalDateTime joinDate = userHasGroup.getDate();
        this.joinedDate = joinDate != null ? formatTimeAgo(joinDate) : "Unknown";
        this.postCount = postCount;
    }
}
