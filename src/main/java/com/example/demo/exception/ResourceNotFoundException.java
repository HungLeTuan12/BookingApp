package com.example.demo.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String mess) {
        super(mess);
    }
}
