package com.example.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotNull
    @Max(value = 20, message = "length of username must smaller 20")
    @Min(value = 6,message = "length of username must longer 6")
    public String userName;
    @NotNull
    @Min(value = 6,message = "length of fullName must longer 6")
    public String fullName;
    @NotNull
    @Min(value = 6,message = "length of password must longer 6")
    public String password;
    public Long idRole;
    public Long idMajor;
}
