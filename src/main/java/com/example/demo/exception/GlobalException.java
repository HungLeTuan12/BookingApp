package com.example.demo.exception;


import com.example.demo.response.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().size() > 0 ?
                ex.getBindingResult().getAllErrors().get(0).getDefaultMessage().toString() : "Unknown error, please check input";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(errorMessage));
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse<String>> handleMapperJsonExceptions(JsonProcessingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExitsException.class)
    public ResponseEntity<ErrorResponse<String>> handleResourceAlready(ResourceAlreadyExitsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse<>(ex.getMessage()));
    }
}
