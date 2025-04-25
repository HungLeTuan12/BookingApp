package com.example.demo.controller;

import com.example.demo.dto.dto.UserDTO;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.UserModel;
import com.example.demo.response.ErrorResponse;
import com.example.demo.response.SuccessResponse;
import com.example.demo.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try{
            Map<String,String> u = userService.login(loginRequest);
            return ResponseEntity.ok().body(new SuccessResponse<>("Login success",u));
        }catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try{
            UserModel u = userService.register(userDTO);
            return ResponseEntity.ok().body(u);
       }
        catch (ResourceNotFoundException e){
            return ResponseEntity.badRequest().body(new ErrorResponse<>(e.getMessage()));
       }
       catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse<>(e.getMessage()));
        }
    }
}
