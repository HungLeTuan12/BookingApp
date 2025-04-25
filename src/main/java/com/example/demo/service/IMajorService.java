package com.example.demo.service;
import com.example.demo.dto.dto.MajorDTO;
import com.example.demo.entity.Major;
import com.example.demo.model.MajorModel;

import java.util.List;
import java.util.Optional;

public interface IMajorService {
    List<MajorModel> getAll();
    Optional<Major> findById(long id);
    boolean checkNameMajor(String name);
    boolean saveMajor(MajorDTO majorDTO, Long id, String image, String idImage);
    void updateMajor(MajorDTO majorDTO, Long id, String image, String idImage);
    void deleteMajor(Long id);
    void convertDTOtoEntity(MajorDTO majorDTO, Major major);
    MajorModel convertEntityToModel(Major major);
}
