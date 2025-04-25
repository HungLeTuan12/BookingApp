package com.example.demo.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SuccessResponse<T> extends BaseResponse<T> {
    public SuccessResponse(String message) {
        super("success",message,null);
    }

    public SuccessResponse(String message,T data) {
        super("success",message,data);
    }
}
