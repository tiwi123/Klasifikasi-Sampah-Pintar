package com.example.wasteclassification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ClassificationService {

    @Autowired
    private RestTemplate restTemplate;

    private final String FLASK_SERVER_URL = "http://192.168.100.23:5000/classify";

    public String classifyImage(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong.");
        }

        // Simpan gambar ke direktori sementara
        File tempFile = File.createTempFile("upload-", ".jpg");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(image.getBytes());
        }

        try {
            // Membuat request ke Flask backend
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            Resource resource = new InputStreamResource(image.getInputStream()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    FLASK_SERVER_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "Error: Response status " + response.getStatusCode();
            }

        } finally {
            // Hapus file sementara setelah selesai
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
