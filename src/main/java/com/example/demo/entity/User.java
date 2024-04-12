package com.example.demo.entity;

import com.example.demo.enumeration.Role;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder 
@Entity
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String staffId;
    private String name;
    private String dob;
    private String email;
    private String password;
    private String department;
    private String division;
    private String team;
    private String door_log_number;
    private String photo;
    private String interest;
    private String biography;
    private boolean status;

    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OTP otp;

    @JsonBackReference
    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<Post> posts;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHasGroup> userHasGroups;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<Like> likes;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<GroupMessage> groupMessages;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<UserMention> userMentions;

    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER)
    private List<BookMark> bookMarks;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "skill_has_user", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "skill_id")})
    private List<Skill> skills;

    @PrePersist
    public void beforePersist(){
        this.updatedAt = LocalDateTime.now();
    }
}

