package com.example.demo.service;

import com.example.demo.dto.dto.ScheduleDTO;
import com.example.demo.entity.Schedule;
import com.example.demo.model.ScheduleModel;

import java.util.Date;
import java.util.List;

public interface IScheduleService {
    ScheduleModel convertToModel(Schedule schedule);
    List<ScheduleModel> getAllSchedule(Long idDoctor);
    ScheduleModel getAllBy(Date date, Long i);

    List<ScheduleDTO> getDoctorSchedule(Long doctorId, Date startDate, Date endDate);
}
