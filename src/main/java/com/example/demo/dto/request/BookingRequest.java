package com.example.demo.dto.request;


import com.example.demo.constant.Gender;
import lombok.Data;

import java.util.Date;

@Data
public class BookingRequest {
    private Long scheduleId;
    private Long patientId;
    private Long hourId;
    private String fullName;
    private Date dob;
    private String phone;
    private String email;
    private Gender gender;
    private String address;
    private String note;
    private String token;
}
