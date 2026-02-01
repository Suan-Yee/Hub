package com.example.demo.entity;

import com.example.demo.enumeration.Role;
import com.example.demo.enumeration.Access;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
    private boolean roleChangePending;
    private Long triggeredByUserId;
    private LocalDateTime time;
    private boolean turnNoti = false;

    @Enumerated(EnumType.STRING)
    private Access access;

    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OTP otp;

    @JsonIgnore
    @JsonBackReference
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Post> posts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHasGroup> groups;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Like> likes;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Comment> comments;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<BookMark> bookMarks;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "skill_has_user", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = {@JoinColumn(name = "skill_id")})
    private List<Skill> skills;

    @PrePersist
    public void beforePersist(){
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private Set<UserRoom> user_chatRooms;
}

