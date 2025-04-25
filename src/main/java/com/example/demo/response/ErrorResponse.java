package com.example.demo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ErrorResponse<T> extends BaseResponse<T>{
    public ErrorResponse(String message) {
        super("failed",message,null);
    }

    public ErrorResponse(String message,T data){
        super("failed",message,data);
    }
}
