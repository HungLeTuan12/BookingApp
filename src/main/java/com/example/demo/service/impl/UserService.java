package com.example.demo.service.impl;

import com.example.demo.constant.RoleName;
import com.example.demo.dto.dto.UserDTO;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.entity.Major;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceAlreadyExitsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.CustomUserDetail;
import com.example.demo.model.MajorModel;
import com.example.demo.model.UserModel;
import com.example.demo.repository.MajorRepo;
import com.example.demo.repository.RoleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final MajorRepo majorRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public Map<String,String> login(LoginRequest loginRequest) {
        try{
            Map<String,String> map = new HashMap<>();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken((CustomUserDetail) userDetails);
            map.put("accessToken",jwt);
            map.put("roleId",String.valueOf(((CustomUserDetail) userDetails).getRoleId()));
            map.put("userId",String.valueOf(((CustomUserDetail) userDetails).getUserId()));
            return map;
        }catch (BadCredentialsException ex){
            throw new BadCredentialsException("Sai tài khoản hoặc mật khẩu !");
        }
    }

    @Transactional
    public UserModel register(UserDTO userDTO) {
        Optional<Role> role = roleRepo.findById(3L);
        if(!role.isPresent()) throw new ResourceNotFoundException("Invalid id role");
        User u = new User();
        u.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        u.setUsername(userDTO.getUserName());
        u.setFullname(userDTO.getFullName());
        u.setRole(role.get());
        u.setEnabled(true);
        UserModel userModel = convertEntityToModel(u);
        userRepo.save(u);
        return userModel;
    }

    public boolean checkName(String name) {
        return userRepo.findByUsername(name)!=null;
    }

    public void changePass(String pass, Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found Doctor with id :" + id));
        user.setPassword(passwordEncoder.encode(pass));
        userRepo.save(user);
    }

    public void restore(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found Doctor with id :" + id));
        user.setEnabled(true);
        userRepo.save(user);
    }


    public List<UserModel> getAllDoctorBy(Long majorId,String name, Pageable pageable,String status) {
        Boolean enabled = null;
        if (status != null) enabled = status.equals("true");

        // Lấy kết quả từ query
        Page<User> pageResult = userRepo.findByCustom(majorId, status, name, pageable);

        // Lọc và chuyển đổi User thành UserModel
        List<UserModel> userModels = pageResult.getContent().stream()
                .filter(e -> e.getRole().getId() != 1) // Lọc người dùng có Role ID != 1
                .map(this::convertEntityToModel) // Chuyển đổi sang UserModel
                .collect(Collectors.toList());

        // Gán tổng số trang vào từng UserModel
        userModels.forEach(userModel -> userModel.setTotalPages(pageResult.getTotalPages()));

        // Trả về danh sách UserModel
        return userModels;
    }

    public Optional<UserModel> getDoctorById(Long id) {
        return userRepo.findById(id).map(this::convertEntityToModel);
    }

    @Override
    @Transactional
    public void saveDoctor(UserDTO userDTO, String image, String idImage) {
        User user = new User();
        Role role = roleRepo.findById(2L).get();
        Major major = majorRepo.findById(userDTO.getMajorId()).get();
        user.setRole(role);
        user.setMajor(major);
        convertDTOtoEntity(userDTO,user);
        if(idImage != null && image != null) {
            user.setUrlId(idImage);
            user.setAvatar(image);
        }
        user.setTrangthai("dl");
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepo.save(user);
    }


    @Transactional
    public void updateDoctor(UserDTO userDTO, Long id, String image, String idImage) {
        User user = userRepo.findById(id).get();
        if(user.getUsername().equals(userDTO.getUserName())==false && userRepo.findByUsername(userDTO.getUserName())!=null){
            throw new ResourceAlreadyExitsException("UserName đã bị trùng");
        }
        if(image != null && idImage != null) {
            user.setUrlId(idImage);
            user.setAvatar(image);
        }
        Major major = majorRepo.findById(userDTO.getMajorId()).get();
        if(major==null) {throw new ResourceNotFoundException("Không tìm thấy khoa cần lưu");}
        else user.setMajor(major);
        convertDTOtoEntity(userDTO,user);
        if(!userDTO.getTrangthai().equals("dl")) {
            user.setEnabled(false);
            user.setTrangthai(userDTO.getTrangthai());
        }
        else {
            user.setTrangthai("dl");
            user.setEnabled(true);
        }
        userRepo.save(user);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        User user = userRepo.findById(id).get();
        userRepo.deleteById(id);
    }

    public void convertDTOtoEntity(UserDTO userDTO, User user) {
        user.setFullname(userDTO.getFullName());
        user.setUsername(userDTO.getUserName());
        user.setGmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setFee(userDTO.getFee());
        user.setDescription(userDTO.getDescription());
    }


    @Override
    public UserModel convertEntityToModel(User user) {
        MajorModel majorModel = new MajorModel();
        if(user.getMajor()!=null) {
            majorModel.setId(user.getMajor().getId());
            majorModel.setName(user.getMajor().getName());
        }
        UserModel userModel = new UserModel(user.getId(),user.getAvatar(),user.getFullname(),user.getUsername(),user.getPhone(),
                user.getGmail(), user.getDescription(), user.getRole().getId(),user.isEnabled(),majorModel,user.getTrangthai(), user.getFee());
        return userModel;
    }
}
