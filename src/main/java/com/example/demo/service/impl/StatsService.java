package com.example.demo.service.impl;

import com.example.demo.dto.response.StatsResponse;
import com.example.demo.repository.BookingRepo;
import com.example.demo.repository.MajorRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private BookingRepo bookingRepository;

    @Autowired
    private MajorRepo majorRepository;

    public StatsResponse getStats() {
        StatsResponse response = new StatsResponse();

        // 1. Tổng quan
        response.setTotalDoctors(userRepository.countDoctors());
        response.setTotalPatients(bookingRepository.countTotalPatients());
        response.setTotalAppointments(bookingRepository.countTotalAppointments());
        response.setTotalSpecialties(majorRepository.countTotalSpecialties());

        // 2. Xu hướng tăng trưởng
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -29); // 30 ngày
        Date startDateThisMonth = calendar.getTime();

        // Bệnh nhân mới tháng này
        long newPatientsThisMonth = bookingRepository.countNewPatientsThisMonth(startDateThisMonth, endDate);
        response.setNewPatientsThisMonth(newPatientsThisMonth);

        // Lịch hẹn tháng này
        long appointmentsThisMonth = bookingRepository.countAppointmentsThisMonth(startDateThisMonth, endDate);
        response.setAppointmentsThisMonth(appointmentsThisMonth);

        // Tăng trưởng bệnh nhân
        calendar.setTime(startDateThisMonth);
        calendar.add(Calendar.DAY_OF_MONTH, -30); // Tháng trước
        Date startDateLastMonth = calendar.getTime();
        calendar.setTime(startDateThisMonth);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDateLastMonth = calendar.getTime();

        long newPatientsLastMonth = bookingRepository.countNewPatientsBetween(startDateLastMonth, endDateLastMonth);
        double patientGrowth = newPatientsLastMonth > 0
                ? ((double) (newPatientsThisMonth - newPatientsLastMonth) / newPatientsLastMonth) * 100
                : newPatientsThisMonth > 0 ? 100.0 : 0.0;
        response.setPatientGrowth(Math.round(patientGrowth * 100.0) / 100.0);

        // 3. Top chuyên khoa
        List<Object[]> specialtyDistribution = bookingRepository.countAppointmentsBySpecialty();
        List<Map<String, Object>> topSpecialties = specialtyDistribution.stream()
                .limit(3)
                .map(row -> {
                    Map<String, Object> specialty = new HashMap<>();
                    specialty.put("name", (String) row[0]);
                    specialty.put("count", (Long) row[1]);
                    return specialty;
                })
                .collect(Collectors.toList());
        response.setTopSpecialties(topSpecialties);

        // 4. Số bác sĩ theo chuyên khoa
        List<Object[]> doctorsBySpecialty = userRepository.countDoctorsBySpecialty();
        Map<String, Long> doctorsBySpecialtyMap = new HashMap<>();
        for (Object[] row : doctorsBySpecialty) {
            String specialtyName = (String) row[0];
            Long count = (Long) row[1];
            doctorsBySpecialtyMap.put(specialtyName, count);
        }
        response.setDoctorsBySpecialty(doctorsBySpecialtyMap);

        // 5. Lịch hẹn 30 ngày gần nhất
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, -29); // 30 ngày
        Date startDateDays = calendar.getTime();

        List<Object[]> appointmentsOverDays = bookingRepository.countAppointmentsOverDays(startDateDays, endDate);
        List<Long> appointmentsOverDaysList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Long> appointmentsByDay = appointmentsOverDays.stream()
                .collect(Collectors.toMap(
                        row -> sdf.format((Date) row[0]),
                        row -> (Long) row[1]
                ));

        calendar.setTime(startDateDays);
        for (int i = 0; i < 30; i++) {
            String dayStr = sdf.format(calendar.getTime());
            appointmentsOverDaysList.add(appointmentsByDay.getOrDefault(dayStr, 0L));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        response.setAppointmentsOverDays(appointmentsOverDaysList);

        // 6. Bệnh nhân mới 30 ngày gần nhất
        List<Object[]> patientGrowthOverDays = bookingRepository.countNewPatientsOverDays(startDateDays, endDate);
        List<Long> patientGrowthOverDaysList = new ArrayList<>();
        Map<String, Long> patientsByDay = patientGrowthOverDays.stream()
                .collect(Collectors.toMap(
                        row -> sdf.format((Date) row[0]),
                        row -> (Long) row[1]
                ));

        calendar.setTime(startDateDays);
        for (int i = 0; i < 30; i++) {
            String dayStr = sdf.format(calendar.getTime());
            patientGrowthOverDaysList.add(patientsByDay.getOrDefault(dayStr, 0L));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        response.setPatientGrowthOverDays(patientGrowthOverDaysList);

        // 7. Top giờ khám
        List<Object[]> hoursDistribution = bookingRepository.countAppointmentsByHour(startDateDays, endDate);
        List<Map<String, Object>> topHours = hoursDistribution.stream()
                .limit(3)
                .map(row -> {
                    Map<String, Object> hour = new HashMap<>();
                    hour.put("name", (String) row[0]);
                    hour.put("count", (Long) row[1]);
                    return hour;
                })
                .collect(Collectors.toList());
        response.setTopHours(topHours);

        return response;
    }
}
