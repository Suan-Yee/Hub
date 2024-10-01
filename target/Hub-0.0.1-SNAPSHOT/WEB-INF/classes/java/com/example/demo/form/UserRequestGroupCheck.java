package com.example.demo.form;

import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserRequestGroupCheck {

    private Long id;
    private String name;
    private String staffId;
    private boolean isJoined;
    private String status;
    private boolean isAdmin;

    public UserRequestGroupCheck(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.staffId = user.getStaffId();
        this.status = "visitor";
        this.isAdmin = false;
    }
}
