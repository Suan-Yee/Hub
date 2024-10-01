package com.example.demo.form;

import lombok.Data;

@Data
public class UserVotedPollOption {
    private long pollId;
    private long answerId;
}
