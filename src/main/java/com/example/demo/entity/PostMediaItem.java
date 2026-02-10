package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_media_items", indexes = {
    @Index(name = "idx_post_media_items_post_id", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "media_id", nullable = false, length = 64)
    private String mediaId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;
}
