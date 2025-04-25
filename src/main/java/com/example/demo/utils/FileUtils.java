package com.example.demo.utils;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUtils {
    public static void saveFile(String uploadDir, String fileName, MultipartFile
            multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir); // lay dia chi thu muc file
        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try(InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream,filePath, StandardCopyOption.REPLACE_EXISTING);

        }catch (IOException ex) {
            throw new IOException("Could not save file" + fileName, ex);
        }
    }

    public static void cleanDir(String uploadDir) throws IOException {
        Path path = Paths.get(uploadDir);
        try {
            Files.list(path).forEach(e -> {
                if(!Files.isDirectory(e)) {
                    try {
                        Files.delete(e);
                    } catch (IOException ex) {
                        throw new RuntimeException("could not delete file: "+ex);
                    }
                }
            });
        }catch (Exception e) {
            System.out.println("Don't have directory");
        }
    }
}
