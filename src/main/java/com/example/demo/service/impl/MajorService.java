package com.example.demo.service.impl;
import com.example.demo.dto.dto.MajorDTO;
import com.example.demo.entity.Major;
import com.example.demo.exception.ResourceAlreadyExitsException;
import com.example.demo.model.MajorModel;
import com.example.demo.repository.MajorRepo;
import com.example.demo.service.IMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MajorService implements IMajorService {
    private final MajorRepo majorRepo;
    @Override
    public List<MajorModel> getAll(){
        return majorRepo.findAll().stream().map(this::convertEntityToModel).collect(Collectors.toList());
    }
    @Override
    public Optional<Major> findById(long id) {
        return majorRepo.findById(id);
    }
    @Override
    public boolean checkNameMajor(String name) {
        return majorRepo.findByName(name)!=null;
    }

    @Transactional
    @Override
    public boolean saveMajor(MajorDTO majorDTO, Long id, String image, String idImage) {
        Major ma = new Major();
        if(id!=null) ma.setId(id);
        convertDTOtoEntity(majorDTO,ma);
        if(idImage != null && image != null) {
            ma.setIdImage(idImage);
            ma.setAvatar(image);
        }
        Major major = majorRepo.save(ma);
        return major != null;
    }

    @Transactional
    @Override
    public void updateMajor(MajorDTO majorDTO, Long id, String image, String idImage) {
        Major major = majorRepo.findById(id).get();
        if(major.getName() != major.getName() && checkNameMajor(majorDTO.getName())) {
            throw new ResourceAlreadyExitsException("Tên khoa đã trùng với 1 khoa khác");
        }
        major.setName(majorDTO.getName());
        major.setDescription(majorDTO.getDescription());
        if(image != null && idImage != null) {
            major.setIdImage(idImage);
            major.setAvatar(image);
        }
        majorRepo.save(major);
    }

    @Transactional
    @Override
    public void deleteMajor(Long id) {
        majorRepo.deleteById(id);
    }

    @Override
    public void convertDTOtoEntity(MajorDTO majorDTO, Major major) {
        major.setName(majorDTO.getName());
        major.setDescription(majorDTO.getDescription());
    }

    @Override
    public MajorModel convertEntityToModel(Major major) {
        MajorModel model = new MajorModel();
        model.setId(major.getId());
        model.setName(major.getName());
        model.setDescription(major.getDescription());
        model.setImage(major.getAvatar());
        return model;
    }
}
