package com.example.demo.repository;

import com.example.demo.entity.ScheduleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleUserRepo extends JpaRepository<ScheduleUser,Long> {
    List<ScheduleUser> findAllByUserId(Long id);

    Optional<ScheduleUser> deleteByUser_IdAndSchedule_Id(Long id1, Long id2);

    Optional<ScheduleUser> findByUser_IdAndSchedule_Id(Long idUser, Long idSche);
}
