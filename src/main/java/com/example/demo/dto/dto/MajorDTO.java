package com.example.demo.dto.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MajorDTO {
    @NotBlank(message = "Name can not blank")
    @Size(max = 35,message = "Length of name must shorter 35")
    private String name;
    @Size(max = 64,message = "Length of description must longer 64")
    private String description;
}
