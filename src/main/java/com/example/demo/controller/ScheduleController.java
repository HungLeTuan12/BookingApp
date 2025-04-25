package com.example.demo.controller;

import com.example.demo.dto.dto.AvailableTimeSlotDTO;
import com.example.demo.dto.dto.ScheduleDTO;
import com.example.demo.dto.request.ScheduleCreateRequest;
import com.example.demo.service.impl.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedule/doctor/{doctorId}")
    public ResponseEntity<List<ScheduleDTO>> getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<ScheduleDTO> schedules = scheduleService.getDoctorSchedule(doctorId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }
    @GetMapping("/schedule/doctor/{doctorId}/available-slots")
    public ResponseEntity<List<AvailableTimeSlotDTO>> getAvailableDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<AvailableTimeSlotDTO> schedules = scheduleService.getAvailableTimeSlots(doctorId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/schedule/major/{majorId}/available-slots")
    public ResponseEntity<List<AvailableTimeSlotDTO>> getAvailableMajorSchedule(
            @PathVariable Long majorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<AvailableTimeSlotDTO> schedules = scheduleService.getAvailableTimeSlotsForMajor(majorId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleCreateRequest request) {
        try {
            if (request.getDoctorId() == null || request.getDate() == null || request.getHourId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Thiếu thông tin bắt buộc. Vui lòng cung cấp doctorId, date, và hourId.");
            }

            scheduleService.createSchedule(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi tạo lịch: " + e.getMessage());
        }
    }

    @PutMapping("/schedule")
    public ResponseEntity<?> updateSchedule(@RequestBody ScheduleCreateRequest request) {
        try {
            if (request.getId() == null || request.getDoctorId() == null || request.getDate() == null || request.getHourId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Thiếu thông tin bắt buộc. Vui lòng cung cấp id, doctorId, date, và hourId.");
            }

            scheduleService.updateSchedule(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi cập nhật lịch: " + e.getMessage());
        }
    }

    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi xóa lịch: " + e.getMessage());
        }
    }
}