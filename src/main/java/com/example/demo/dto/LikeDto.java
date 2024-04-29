package com.example.demo.dto;

import com.example.demo.entity.Like;
import com.example.demo.entity.Post;
import com.example.demo.enumeration.LikeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class LikeDto {

    private Long id;
    @Enumerated(EnumType.STRING)
    private LocalDateTime date;
    private boolean status;
    private Long postId;
}
