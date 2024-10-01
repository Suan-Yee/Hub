package com.example.demo.form;

import lombok.*;

@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
public class UserCommentForm {

    private Long id;
    private String name;
    private String image;
}
