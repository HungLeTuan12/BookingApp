package com.example.demo.repository;

import com.example.demo.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorRepo extends JpaRepository<Major, Long> {
    Major findByName(String name);

    // Đếm tổng số chuyên khoa
    @Query("SELECT COUNT(m) FROM Major m")
    long countTotalSpecialties();
}
