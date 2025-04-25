package com.example.demo.service;

import com.example.demo.dto.dto.UserDTO;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.model.UserModel;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    Map<String,String> login(LoginRequest loginRequest);
    boolean checkName(String name);
    void changePass(String pass, Long id);
    void restore(Long id);
    List<UserModel> getAllDoctorBy(Long majorId, String name, Pageable pageable, String status);
    Optional<UserModel> getDoctorById(Long id);
    void saveDoctor(UserDTO userDTO, String image, String idImage);
    void deleteDoctor(Long id);
    void updateDoctor(UserDTO userDTO, Long id, String image, String idImage);
    UserModel convertEntityToModel(User user);
    void convertDTOtoEntity(UserDTO userDTO, User user);
}
