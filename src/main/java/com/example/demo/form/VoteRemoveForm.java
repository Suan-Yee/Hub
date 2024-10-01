package com.example.demo.form;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRemoveForm {

    private Long answerId;
    private Long pollId;
}
