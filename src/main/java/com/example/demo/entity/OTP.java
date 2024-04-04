package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Entity
public class OTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private String otpCode;
    private LocalDateTime createdDate;
    private LocalDateTime expireDate;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void beforePersist(){
        this.createdDate = LocalDateTime.now();
        this.expireDate = createdDate.plusMinutes(5);
    }
}

