package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.*;

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
