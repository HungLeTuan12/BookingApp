package com.example.demo.controller;

import com.example.demo.constant.RoleName;
import com.example.demo.dto.dto.UserDTO;
import com.example.demo.entity.Major;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceAlreadyExitsException;
import com.example.demo.model.UserModel;
import com.example.demo.repository.MajorRepo;
import com.example.demo.repository.RoleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.response.ErrorResponse;
import com.example.demo.response.SuccessResponse;
import com.example.demo.service.impl.CloudinaryService;
import com.example.demo.service.impl.MajorService;
import com.example.demo.service.impl.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MajorService majorService;
    private final CloudinaryService cloudinaryService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final MajorRepo majorRepo;


    @GetMapping("/doctor/all")
    public ResponseEntity<?> getAllDoctor() {
        try{
            List<User> doctors = userRepo.findAllByRole();
            List<UserDTO> userDTOS = new ArrayList<>();
            for(User user : doctors) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setFullName(user.getFullname());
                userDTO.setMajorId(user.getMajor().getId());
                userDTO.setMajorName(user.getMajor().getName());
                userDTO.setFee(user.getFee());
                userDTOS.add(userDTO);
            }
            return ResponseEntity.ok().body(new SuccessResponse<>("Get data success",userDTOS));
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>("Server Error"));
        }
    }
    @GetMapping("/doctor/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        try{
            Optional<UserModel> userModel = userService.getDoctorById(id);
            if(!userModel.isPresent()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Not found doctor have id: " + id));
            }
            return ResponseEntity.ok().body(new SuccessResponse<>("Get data success",userModel.get()));
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>("Server Error"));
        }
    }
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctor(@RequestParam(value = "page",defaultValue = "1") int page,
                                          @RequestParam(value = "size",defaultValue = "100") int size,
                                          @RequestParam(value = "majorId",required = false) Long majorId,
                                          @RequestParam(value = "name",required = false) String name,
                                          @RequestParam(value = "status" ,required = false) String status) {
        try {
            if(majorId != null && !majorService.findById(majorId).isPresent()){
                return ResponseEntity.badRequest().body(new ErrorResponse<>("Not found Id Major"));
            }
            Pageable pageable = PageRequest.of(page-1,size);
            List<UserModel> list = userService.getAllDoctorBy(majorId,name,pageable,status);
            List<User> users = userRepo.findAll();

            return ResponseEntity.ok().body(new SuccessResponse<>("Get data success",list));
        }catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>("Server Error"));
        }
    }
    @PostMapping(value = "doctor")
    public ResponseEntity<?> createDoctor(@RequestPart("file") MultipartFile multipartFile,
                                          @RequestPart("doctorDto") String object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytesGiaiMa = Base64.getDecoder().decode(object);
            String giaiMaBase64 = new String(bytesGiaiMa);
            UserDTO userDTO = objectMapper.readValue(giaiMaBase64,UserDTO.class);
            if(userService.checkName(userDTO.getUserName())) {
                return ResponseEntity.badRequest().body(new ErrorResponse<>("UserName đã tồn tại!!"));
            }
            Map data = cloudinaryService.upload(multipartFile);
            userService.saveDoctor(userDTO,data.get("url").toString(),data.get("public_id").toString());
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã tạo thành công"));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PutMapping(value = "doctor/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateDoctor(@RequestPart(value = "file",required = false) MultipartFile multipartFile,
                                          @RequestPart(value = "doctorDto") String object,
                                          @PathVariable("id") Long id) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytesGiaiMa = Base64.getDecoder().decode(object);
            String giaiMaBase64 = new String(bytesGiaiMa);
            UserDTO userDTO = objectMapper.readValue(giaiMaBase64,UserDTO.class);
            System.out.println(userDTO);
            String image = null, idImage = null;
            User user = userRepo.findById(id).get();
            if(user==null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Doctor không tồn tại!!!"));
            }
            if(multipartFile!=null){
                Map data = cloudinaryService.upload(multipartFile);
                cloudinaryService.delete(user.getUrlId());
                image = data.get("url").toString();
                idImage = data.get("public_id").toString();
                userService.updateDoctor(userDTO,id,image,idImage);
            }
            userService.updateDoctor(userDTO,id,null,null);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }
        catch (ResourceAlreadyExitsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse<>(ex.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/change-pass/{id}")
    public ResponseEntity<?> changePass(@PathVariable("id") Long id,
                                        @RequestBody String pass) {
        try{
            userService.changePass(pass,id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("restore-status/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> restoreStatus(@PathVariable("id") Long id){
        try{
            userService.restore(id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã cập nhật thành công"));
        }catch (Exception ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @DeleteMapping(value = "doctor/{id}")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable("id") Long id) {
        try{
            Optional<User> user = userRepo.findById(id);
            if(user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>("Id Doctor không tồn tại!!!"));
            }
            if(!user.get().isEnabled()) cloudinaryService.delete(user.get().getUrlId());
            userService.deleteDoctor(id);
            return ResponseEntity.ok().body(new SuccessResponse<>("Đã xóa thành công Doctor có id: " + id));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>(e.getMessage()));
        }
    }
    // API lấy danh sách bác sĩ theo chuyên khoa (major)
    @GetMapping("/doctors/by-major/{majorId}")
    public List<User> getDoctorsByMajor(@PathVariable Long majorId) {
        Major major = majorRepo.findById(majorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyên khoa với ID: " + majorId));
        Role doctorRole = roleRepo.findByName(RoleName.ROLE_DOCTOR);
        return userRepo.findByRoleAndMajor(doctorRole, major);
    }
}
