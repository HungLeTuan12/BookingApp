package com.example.demo.service.impl;

import com.example.demo.constant.RoleName;
import com.example.demo.dto.dto.AvailableTimeSlotDTO;
import com.example.demo.dto.dto.ScheduleDTO;
import com.example.demo.dto.request.ScheduleCreateRequest;
import com.example.demo.entity.*;
import com.example.demo.model.ScheduleModel;
import com.example.demo.repository.HourRepo;
import com.example.demo.repository.MajorRepo;
import com.example.demo.repository.ScheduleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.IScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService implements IScheduleService {
    private final ScheduleRepo scheduleRepo;
    private final UserRepo userRepo;
    private final HourRepo hourRepo;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final MajorRepo majorRepo;

    @Override
    public ScheduleModel convertToModel(Schedule schedule) {
        return null;
    }

    @Override
    public List<ScheduleModel> getAllSchedule(Long idDoctor) {
        return null;
    }



    @Override
    public ScheduleModel getAllBy(Date date, Long i) {
        return null;
    }

    @Override
    public List<ScheduleDTO> getDoctorSchedule(Long doctorId, Date startDate, Date endDate) {
        List<Schedule> schedules = scheduleRepo.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
        return schedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<AvailableTimeSlotDTO> getAvailableTimeSlots(Long doctorId, Date startDate, Date endDate) {
        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + doctorId));

        List<Schedule> schedules = scheduleRepo.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
        List<AvailableTimeSlotDTO> timeSlots = new ArrayList<>();

        for (Schedule schedule : schedules) {
            if (schedule.getIsBooked()) {
                continue; // Bỏ qua lịch đã được đặt
            }

            String dateStr = dateFormat.format(schedule.getDate());
            Hour hour = schedule.getHour();
            AvailableTimeSlotDTO timeSlot = new AvailableTimeSlotDTO();
            timeSlot.setScheduleId(schedule.getId());
            timeSlot.setDate(dateStr);
            timeSlot.setHourId(hour.getId());
            timeSlot.setHourName(hour.getName());
            timeSlot.setFee(doctor.getFee());
            timeSlots.add(timeSlot);
        }

        return timeSlots;
    }

    public List<AvailableTimeSlotDTO> getAvailableTimeSlotsForMajor(Long majorId, Date startDate, Date endDate) {
        Major major = majorRepo.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa với ID: " + majorId));

        List<Schedule> schedules = scheduleRepo.findByMajorIdAndDateBetween(majorId, startDate, endDate);
        List<AvailableTimeSlotDTO> timeSlots = new ArrayList<>();

        for (Schedule schedule : schedules) {
            if (schedule.getIsBooked()) {
                continue; // Bỏ qua lịch đã được đặt
            }

            String dateStr = dateFormat.format(schedule.getDate());
            Hour hour = schedule.getHour();
            AvailableTimeSlotDTO timeSlot = new AvailableTimeSlotDTO();
            timeSlot.setScheduleId(schedule.getId());
            timeSlot.setDate(dateStr);
            timeSlot.setHourId(hour.getId());
            timeSlot.setHourName(hour.getName());
            timeSlot.setDoctorName(schedule.getDoctor().getFullname());
            timeSlot.setDoctorId(schedule.getDoctor().getId());
            timeSlot.setFee(schedule.getDoctor().getFee());
            timeSlots.add(timeSlot);
        }

        return timeSlots;
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        String dateStr = dateFormat.format(schedule.getDate());
        dto.setDate(dateStr);

        Hour hour = schedule.getHour();
        dto.setHourId(hour.getId()); // Trả về giá trị đơn
        dto.setHourName(hour.getName()); // Trả về giá trị đơn

        dto.setDoctorId(schedule.getDoctor().getId());
        dto.setDoctorName(schedule.getDoctor().getFullname());
        dto.setIsBooked(schedule.getIsBooked());
        return dto;
    }

    @Transactional
    public void createSchedule(ScheduleCreateRequest request) {
        User doctor = userRepo.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        if (doctor.getRole().getName() != RoleName.ROLE_DOCTOR) {
            throw new RuntimeException("Người này không phải là bác sĩ");
        }

        Date scheduleDate;
        try {
            scheduleDate = dateFormat.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ: " + request.getDate());
        }

        Hour hour = hourRepo.findById(request.getHourId())
                .orElseThrow(() -> new RuntimeException("Khung giờ với ID " + request.getHourId() + " không tồn tại"));

        Optional<Schedule> existingSchedule = scheduleRepo.findByDoctorIdAndDateAndHour(
                request.getDoctorId(), scheduleDate, hour);
        if (existingSchedule.isPresent()) {
            throw new RuntimeException("Lịch đã tồn tại cho bác sĩ này vào ngày " + request.getDate() +
                    " và khung giờ " + hour.getName());
        }

        Schedule schedule = new Schedule();
        schedule.setDate(scheduleDate);
        schedule.setHour(hour);
        schedule.setDoctor(doctor);
        schedule.setIsBooked(false);

        scheduleRepo.save(schedule);
    }

    @Transactional
    public void updateSchedule(ScheduleCreateRequest request) {
        Schedule schedule = scheduleRepo.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + request.getId()));

        User doctor = userRepo.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với ID: " + request.getDoctorId()));

        if (doctor.getRole().getName() != RoleName.ROLE_DOCTOR) {
            throw new RuntimeException("Người này không phải là bác sĩ");
        }

        Date scheduleDate;
        try {
            scheduleDate = dateFormat.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ: " + request.getDate());
        }

        Hour newHour = hourRepo.findById(request.getHourId())
                .orElseThrow(() -> new RuntimeException("Khung giờ với ID " + request.getHourId() + " không tồn tại"));

        Optional<Schedule> existingSchedule = scheduleRepo.findByDoctorIdAndDateAndHour(
                doctor.getId(), scheduleDate, newHour);
        if (existingSchedule.isPresent() && !existingSchedule.get().getId().equals(schedule.getId())) {
            throw new RuntimeException("Bác sĩ đã có lịch với khung giờ này vào ngày " + request.getDate());
        }

        schedule.setDate(scheduleDate);
        schedule.setDoctor(doctor);
        schedule.setHour(newHour);
        schedule.setIsBooked(false);

        scheduleRepo.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch với ID: " + scheduleId));

        if (schedule.getIsBooked()) {
            throw new RuntimeException("Không thể xóa lịch đã được đặt!");
        }

        scheduleRepo.delete(schedule);
    }
}