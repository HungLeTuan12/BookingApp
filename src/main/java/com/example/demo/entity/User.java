package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String fullname;

    @Column(length = 100,unique = true,nullable = false)
    private String username;

    @Column(length = 100,nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 50)
    private String gmail;

    @Column(length = 100)
    private String avatar;

    @Column(length = 20)
    private String trangthai;

    @Column(length = 100)
    private String urlId;

    @Column(length = 255)
    private String description;

    private boolean enabled;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;


    @ManyToOne
    @JoinColumn(name = "role_id",referencedColumnName = "id")
    @JsonIgnore
    private Role role;

    @ManyToOne
    @JoinColumn(name = "major_id",referencedColumnName = "id")
    @JsonIgnore
    private Major major;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal fee; // Thêm trường chi phí khám

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Booking> listBooking = new ArrayList<>();
}
