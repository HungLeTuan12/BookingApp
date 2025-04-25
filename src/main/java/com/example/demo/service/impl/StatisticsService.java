package com.example.demo.service.impl;

import com.example.demo.dto.dto.StatisticsDTO;
import com.example.demo.repository.BookingRepo;
import com.example.demo.repository.ScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private BookingRepo bookingRepository;

    @Autowired
    private ScheduleRepo scheduleRepository;

    private final Map<Long, String> hourNames = new LinkedHashMap<>();

    public StatisticsService() {
        // Khởi tạo hourNames theo thứ tự thời gian
        hourNames.put(1L, "7h - 8h");
        hourNames.put(2L, "8h - 9h");
        hourNames.put(3L, "9h - 10h");
        hourNames.put(4L, "10h - 11h");
        hourNames.put(5L, "13h - 14h");
        hourNames.put(6L, "14h - 15h");
        hourNames.put(7L, "15h - 16h");
        hourNames.put(8L, "16h - 17h");
    }

    private final Map<String, String> dayOfWeekNames = new LinkedHashMap<>();

    {
        // Khởi tạo dayOfWeekNames theo thứ tự ngày trong tuần
        dayOfWeekNames.put("Monday", "Thứ 2");
        dayOfWeekNames.put("Tuesday", "Thứ 3");
        dayOfWeekNames.put("Wednesday", "Thứ 4");
        dayOfWeekNames.put("Thursday", "Thứ 5");
        dayOfWeekNames.put("Friday", "Thứ 6");
        dayOfWeekNames.put("Saturday", "Thứ 7");
        dayOfWeekNames.put("Sunday", "CN");
    }

    public StatisticsDTO getStatistics(Long doctorId, Date startDate, Date endDate) {
        StatisticsDTO stats = new StatisticsDTO();

        // Thống kê lịch hẹn theo trạng thái
        Map<String, Integer> bookingStatusStats = new LinkedHashMap<>();
        bookingStatusStats.put("PENDING", 0);
        bookingStatusStats.put("SUCCESS", 0);
        bookingStatusStats.put("FAILURE", 0);
        List<Object[]> statusResults = bookingRepository.countBookingsByStatus(doctorId, startDate, endDate);
        for (Object[] result : statusResults) {
            String status = result[0].toString();
            Integer count = ((Number) result[1]).intValue();
            bookingStatusStats.put(status, count);
        }
        stats.setBookingStatusStats(bookingStatusStats);

        // Thống kê lịch hẹn theo khung giờ
        Map<String, Integer> bookingHourStats = new LinkedHashMap<>();
        for (Long hourId = 1L; hourId <= 8L; hourId++) {
            bookingHourStats.put(hourNames.get(hourId), 0);
        }
        List<Object[]> hourResults = bookingRepository.countBookingsByHour(doctorId, startDate, endDate);
        for (Object[] result : hourResults) {
            Long hourId = ((Number) result[0]).longValue();
            Integer count = ((Number) result[1]).intValue();
            bookingHourStats.put(hourNames.get(hourId), count);
        }
        stats.setBookingHourStats(bookingHourStats);

        // Tỷ lệ lịch làm việc đã được đặt
        Map<String, Integer> scheduleBookingRatio = new LinkedHashMap<>();
        Long totalSchedules = scheduleRepository.countSchedules(doctorId, startDate, endDate);
        Long bookedSchedules = scheduleRepository.countBookedSchedules(doctorId, startDate, endDate);
        scheduleBookingRatio.put("booked", bookedSchedules.intValue());
        scheduleBookingRatio.put("notBooked", (int)(totalSchedules - bookedSchedules));
        stats.setScheduleBookingRatio(scheduleBookingRatio);

        // Thống kê lịch hẹn theo ngày trong tuần
        Map<String, Integer> bookingDayOfWeekStats = new LinkedHashMap<>();
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
        for (String day : days) {
            bookingDayOfWeekStats.put(day, 0);
        }
        List<Object[]> dayOfWeekResults = bookingRepository.countBookingsByDayOfWeek(doctorId, startDate, endDate);
        for (Object[] result : dayOfWeekResults) {
            String dayName = dayOfWeekNames.get(result[0].toString());
            Integer count = ((Number) result[1]).intValue();
            bookingDayOfWeekStats.put(dayName, count);
        }
        stats.setBookingDayOfWeekStats(bookingDayOfWeekStats);

        return stats;
    }
}