package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_user_id", columnList = "user_id"),
    @Index(name = "idx_posts_group_id", columnList = "group_id"),
    @Index(name = "idx_posts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    
    @Column(nullable = false, length = 20)
    private String type; // 'text', 'image', 'video', 'poll', 'repost'
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "position")
    @Builder.Default
    private List<PostMediaItem> mediaItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "position")
    @Builder.Default
    private List<PostMention> mentions = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "position")
    @Builder.Default
    private List<PostTag> tags = new ArrayList<>();
    
    @Column(length = 100)
    private String location;
    
    @Column(length = 20)
    private String visibility = "public"; // 'public', 'followers', 'me'
    
    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @Column(name = "comments_count")
    private Integer commentsCount = 0;
    
    @Column(name = "shares_count")
    private Integer sharesCount = 0;
    
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;
    
    @Column(name = "edited")
    private Boolean edited = false;
    
    @Column(name = "poll_question", columnDefinition = "TEXT")
    private String pollQuestion;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PollOption> pollOptions = new HashSet<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();
    
    @OneToMany(mappedBy = "originalPost")
    @Builder.Default
    private Set<Post> reposts = new HashSet<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Bookmark> bookmarks = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "post_hashtags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    @Builder.Default
    private Set<Hashtag> hashtags = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
        edited = true;
    }
    
}
