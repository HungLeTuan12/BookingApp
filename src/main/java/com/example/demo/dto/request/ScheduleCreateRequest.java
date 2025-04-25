package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleCreateRequest {
    private Long id;
    private Long doctorId;
    private String date; // Định dạng YYYY-MM-DD
    private Long hourId;
}

