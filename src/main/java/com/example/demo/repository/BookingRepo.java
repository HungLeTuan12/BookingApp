package com.example.demo.repository;

import com.example.demo.constant.Status;
import com.example.demo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Long> {
    @Query("SELECT u FROM Booking u " +
            "WHERE (:id IS NULL OR u.user.id = :id) " +
            "AND (:status IS NULL OR u.status = :status)" +
            "AND (:start IS NULL OR u.date >= :start) " +
            "AND (:end IS NULL OR u.date <= :end) " +
            "order by u.date")
    List<Booking> findByCustom(Long id, Status status, Date start, Date end);

    @Query("SELECT b FROM Booking b WHERE b.token = :token " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:startDate IS NULL OR b.date >= :startDate) " +
            "AND (:endDate IS NULL OR b.date <= :endDate)")
    List<Booking> findBookingsByTokenFiltered(
            @Param("token") String token,
            @Param("status") Status status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    boolean existsByToken(String token);
    @Query("SELECT b FROM Booking b WHERE b.token = :token ORDER BY b.id DESC")
    List<Booking> findByTokennAndStatus(String token);

    Optional<Object> findByToken(String token);

    List<Booking> findByUserIdAndStatus(Long doctorId, Status status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :doctorId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:startDate IS NULL OR b.date >= :startDate) " +
            "AND (:endDate IS NULL OR b.date <= :endDate) " +
            "AND (:searchTerm IS NULL OR " +
            "LOWER(b.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Booking> findBookingsByDoctorWithFilters(
            @Param("doctorId") Long doctorId,
            @Param("status") Status status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("searchTerm") String searchTerm);
    // Thống kê lịch hẹn theo trạng thái
    @Query("SELECT b.status, COUNT(b) FROM Booking b WHERE b.user.id = :doctorId " +
            "AND (:startDate IS NULL OR b.date >= :startDate) " +
            "AND (:endDate IS NULL OR b.date <= :endDate) " +
            "GROUP BY b.status")
    List<Object[]> countBookingsByStatus(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // Thống kê lịch hẹn theo khung giờ
    @Query("SELECT b.idHour, COUNT(b) FROM Booking b WHERE b.user.id = :doctorId " +
            "AND (:startDate IS NULL OR b.date >= :startDate) " +
            "AND (:endDate IS NULL OR b.date <= :endDate) " +
            "GROUP BY b.idHour")
    List<Object[]> countBookingsByHour(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // Thống kê lịch hẹn theo ngày trong tuần
    @Query("SELECT FUNCTION('DAYNAME', b.date), COUNT(b) FROM Booking b WHERE b.user.id = :doctorId " +
            "AND (:startDate IS NULL OR b.date >= :startDate) " +
            "AND (:endDate IS NULL OR b.date <= :endDate) " +
            "GROUP BY FUNCTION('DAYNAME', b.date)")
    List<Object[]> countBookingsByDayOfWeek(
            @Param("doctorId") Long doctorId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

}
