package com.example.demo.repository;

import com.example.demo.entity.Major;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u " +
            "WHERE (:id IS NULL OR u.major.id = :id) " +
            "AND (:status IS NULL OR u.trangthai = :status) " +
            "AND (:fullname IS NULL OR u.fullname LIKE %:fullname%)")
    Page<User> findByCustom(Long id, String status, String fullname, Pageable pageable);

    List<User> findByRoleAndMajor(Role doctorRole, Major major);
    @Query("SELECT s FROM User s WHERE s.role.id = 2")
    List<User> findAllByRole();

    // Đếm số bác sĩ
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = 'ROLE_DOCTOR'")
    long countDoctors();

    // Đếm số bác sĩ theo chuyên khoa
    @Query("SELECT m.name, COUNT(u) as count " +
            "FROM User u JOIN u.major m " +
            "WHERE u.role.name = 'ROLE_DOCTOR' " +
            "GROUP BY m.id, m.name")
    List<Object[]> countDoctorsBySpecialty();
}