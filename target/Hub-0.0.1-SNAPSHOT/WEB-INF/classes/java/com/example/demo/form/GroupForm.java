package com.example.demo.form;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GroupForm {

    private Long id;
    private String groupPhoto;
    private int totalPost;
    private int totalMember;
    private String groupName;
    private List<String> userImages;
}
