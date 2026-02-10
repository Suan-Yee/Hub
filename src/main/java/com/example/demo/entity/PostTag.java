package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tags", indexes = {
    @Index(name = "idx_post_tags_post_id", columnList = "post_id"),
    @Index(name = "idx_post_tags_tag", columnList = "tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;
}
