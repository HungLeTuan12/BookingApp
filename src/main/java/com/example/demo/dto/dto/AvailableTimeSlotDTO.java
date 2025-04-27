package com.example.demo.dto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AvailableTimeSlotDTO {
    private Long scheduleId; // ID của Schedule chứa khung giờ này
    private String date; // Ngày của lịch
    private Long hourId; // ID của khung giờ
    private String hourName; // Tên khung giờ (ví dụ: "14h - 15h")
    private String doctorName; // Tên khung giờ (ví dụ: "14h - 15h")
    private Long doctorId; // Tên khung giờ (ví dụ: "14h - 15h")
    private BigDecimal fee;
}
