package com.example.demo.entity;

import com.example.demo.enumeration.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder @ToString
@Entity
@Table(name = "user")
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

    @OneToMany(mappedBy = "user")
    private List<Post> posts;

    @OneToMany(mappedBy = "user")
    private List<UserHasGroup> userHasGroups;

    @OneToMany(mappedBy = "user")
    private List<Like> likes;

    @PrePersist
    public void beforePersist(){
        this.updatedAt = LocalDateTime.now();
    }
}

