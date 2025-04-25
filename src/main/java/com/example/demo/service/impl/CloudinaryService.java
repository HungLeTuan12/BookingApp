package com.example.demo.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map upload(MultipartFile multipartFile) throws IOException {
        Map data = this.cloudinary.uploader().upload(multipartFile.getBytes(),Map.of());
        return data;
    }
    public Map delete(String id) throws IOException {
        Map data = this.cloudinary.uploader().destroy(id,Map.of());
        return data;
    }
}
