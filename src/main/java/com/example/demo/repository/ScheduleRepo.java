package com.example.demo.repository;

import com.example.demo.entity.Hour;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepo extends JpaRepository<Schedule, Long> {




    @Query("SELECT s FROM Schedule s WHERE s.doctor.id = :doctorId " +
            "AND DATE(s.date) BETWEEN DATE(:startDate) AND DATE(:endDate)")
    List<Schedule> findByDoctorIdAndDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    @Query("SELECT s " +
            "FROM Schedule s LEFT JOIN User u ON s.doctor.id = u.id " +
            "LEFT JOIN Major m ON s.doctor.major.id = m.id " +
            "LEFT JOIN Hour h ON s.hour.id = h.id " +
            "WHERE s.isBooked = false AND DATE(s.date) BETWEEN DATE(:startDate) AND DATE(:endDate) AND s.doctor.major.id = :majorId " +
            "ORDER BY s.hour.id asc")
    List<Schedule> findByMajorIdAndDateBetween(
            @Param("majorId") Long majorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
    List<Schedule> findByDoctorAndDateBetween(User doctor, Date startDate, Date endDate);

    List<Schedule> findByDoctorId(Long idDoctor);

    Optional<Schedule> findByDoctorIdAndDateAndHour(Long id, Date scheduleDate, Hour newHour);

    // Đếm tổng số lịch làm việc
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.doctor.id = :doctorId " +
            "AND (:startDate IS NULL OR s.date >= :startDate) " +
            "AND (:endDate IS NULL OR s.date <= :endDate)")
    Long countSchedules(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // Đếm số lịch làm việc đã được đặt
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.doctor.id = :doctorId " +
            "AND s.isBooked = true " +
            "AND (:startDate IS NULL OR s.date >= :startDate) " +
            "AND (:endDate IS NULL OR s.date <= :endDate)")
    Long countBookedSchedules(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


}
