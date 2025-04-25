package com.example.demo.dto.dto;

import com.example.demo.constant.Status;
import lombok.Data;

import java.util.Date;

@Data
public class BookingDTO {
    private Long id;
    private String fullName;
    private Date dob;
    private String phone;
    private String email;
    private String gender;
    private String address;
    private Date date;
    private Long idHour;
    private Status status;
    private String note;
    private String token;
}