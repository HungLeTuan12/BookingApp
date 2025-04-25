package com.example.demo.entity;

import com.example.demo.constant.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private Date dob;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private Date date;
    private Long idHour;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String note;
    private String token;

    @Column(name = "created_at")
    private Date createdAt; // Thêm trường createdAt

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Bác sĩ

    @OneToOne
    @JoinColumn(name = "schedule_id")
    @JsonIgnore
    private Schedule schedule;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date(); // Tự động set createdAt khi tạo mới
    }
}