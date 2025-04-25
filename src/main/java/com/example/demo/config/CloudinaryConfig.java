package com.example.demo.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    private final String CLOUD_NAME;
    private final String API_KEY;
    private final String API_SECRET;
    public CloudinaryConfig(@Value("${cloudinary.cloud_name}") String cloudName,
                            @Value("${cloudinary.api_key}") String apiKey,
                            @Value("${cloudinary.api_secret}") String apiSecret){
        this.CLOUD_NAME = cloudName;
        this.API_KEY = apiKey;
        this.API_SECRET = apiSecret;
    }
    @Bean
    public Cloudinary cloudinary(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name",CLOUD_NAME);
        config.put("api_key",API_KEY);
        config.put("api_secret",API_SECRET);

        return new Cloudinary(config);
    }
}
