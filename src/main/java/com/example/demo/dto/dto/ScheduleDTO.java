package com.example.demo.dto.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class ScheduleDTO {
    private Long id;
    private String date; // Định dạng YYYY-MM-DD
    private Long hourId; // Danh sách hourId
    private String hourName; // Danh sách hourName
    private Long doctorId;
    private String doctorName;
    private Boolean isBooked;
}
