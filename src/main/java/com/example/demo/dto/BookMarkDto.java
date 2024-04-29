package com.example.demo.dto;

import com.example.demo.entity.BookMark;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class BookMarkDto {

    private Long id;
    private Long userId;
    private Long postId;
    private boolean status;

    public BookMarkDto(BookMark bookMark){
        this.id = bookMark.getId();
        this.userId = bookMark.getUser().getId();
        this.postId = bookMark.getPost().getId();
        this.status = bookMark.isStatus();
    }

}
