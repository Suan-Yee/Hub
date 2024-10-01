package com.example.demo.form;

import lombok.Data;

import java.util.List;

@Data
public class PollCreationForm {
    private String pollQuestion;
    private List<String> pollOptions;
    private String duration;
    private long groupId;
}
