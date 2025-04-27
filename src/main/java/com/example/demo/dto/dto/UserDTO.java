package com.example.demo.dto.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    @NotBlank(message = "Full name can not blank")
    @Size(min = 6,max = 64,message = "Length of fullName must between 6 and 64")
    private String fullName;


    @NotBlank(message = "UserName can not blank")
    @Size(min = 6,max = 64,message = "Length of username must between 6 and 64")
    private String userName;


    @NotBlank(message = "Password can not blank")
    @Size(min = 6,max = 64,message = "Length of password must between 6 and 64")
    private String password;
    private String phone;
    private String description;
    private String email;
    private String trangthai;
    private Long majorId;
    private String majorName;
    private BigDecimal fee;
}
