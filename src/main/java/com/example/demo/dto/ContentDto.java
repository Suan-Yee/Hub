package com.example.demo.dto;

import com.example.demo.entity.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContentDto {

    private Long id;
    private String text;
    private List<Video> videos;
    private List<File> files;
    private List<Image> images;

    public ContentDto(Content content){
        this.id = content.getId();
        this.text = content.getText();
        this.videos = content.getVideos();
        this.files = content.getFiles();
        this.images = content.getImages();
    }

}
