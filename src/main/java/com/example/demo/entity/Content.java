package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "content")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "text", nullable = false)
    private String text;

    @OneToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @OneToMany(mappedBy = "content")
    private List<Video> videos;

    @OneToMany(mappedBy = "content")
    private List<File> files;

    @OneToMany(mappedBy = "content")
    private List<Image> images;

}
