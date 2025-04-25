package com.example.demo.dto.dto;

import lombok.Data;
import java.util.Map;

@Data
public class StatisticsDTO {
    private Map<String, Integer> bookingStatusStats;
    private Map<String, Integer> bookingHourStats;
    private Map<String, Integer> scheduleBookingRatio;
    private Map<String, Integer> bookingDayOfWeekStats;
}
