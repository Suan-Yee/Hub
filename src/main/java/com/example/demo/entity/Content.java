package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "content")
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "text", nullable = false)
    private String text;

    @JsonBackReference
    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
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
