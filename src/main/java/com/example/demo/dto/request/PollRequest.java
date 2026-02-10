package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PollRequest(
    @NotBlank(message = "Poll question is required")
    String question,
    
    @Size(min = 2, max = 10, message = "Poll must have between 2 and 10 options")
    List<String> options
) {
}
