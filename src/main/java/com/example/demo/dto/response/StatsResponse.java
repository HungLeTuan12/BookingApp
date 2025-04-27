package com.example.demo.dto.response;


import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatsResponse {
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long totalSpecialties;
    private long newPatientsThisMonth;
    private long appointmentsThisMonth;
    private double patientGrowth;
    private List<Map<String, Object>> topSpecialties;
    private Map<String, Long> doctorsBySpecialty;
    private List<Long> appointmentsOverDays; // Lịch hẹn 30 ngày
    private List<Long> patientGrowthOverDays; // Bệnh nhân mới 30 ngày
    private List<Map<String, Object>> topHours; // Top 3 giờ khám
}
