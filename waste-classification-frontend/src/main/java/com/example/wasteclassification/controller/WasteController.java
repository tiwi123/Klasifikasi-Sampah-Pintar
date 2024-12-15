package com.example.wasteclassification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/waste")
public class WasteController {

    @Autowired
    private RestTemplate restTemplate;

    // URL backend Python yang menangani prediksi
    private static final String PYTHON_BACKEND_URL = "http://192.168.18.133:5000/classify";

    @PostMapping("/classify")
    public ResponseEntity<?> classifyWaste(@RequestParam("file") MultipartFile file) {
        try {
            // Cek apakah file kosong
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File cannot be empty");
            }

            // Cek tipe file (misalnya, hanya menerima gambar)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be an image");
            }

            // Header untuk request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Membuat body untuk request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new InputStreamResource(file.getInputStream()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            // Membuat entity request dengan body dan headers
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Kirim request ke backend Python menggunakan exchange untuk POST
            ResponseEntity<Map> response = restTemplate.exchange(PYTHON_BACKEND_URL, HttpMethod.POST, requestEntity, Map.class);

            // Tangani respons dari backend
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Memastikan classification dan confidence ada dalam response
                String classification = (String) responseBody.getOrDefault("classification", "Unknown");
                Object confidenceObj = responseBody.get("confidence");
                double confidence = (confidenceObj instanceof Number) ? ((Number) confidenceObj).doubleValue() : 0.0;

                // Mengembalikan respons dengan hasil klasifikasi
                return ResponseEntity.ok(new WasteResponse(classification, confidence));
            }

            // Jika respons tidak sesuai, kembalikan status error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get a valid response from the backend");
        } catch (IOException e) {
            // Log error dan kembalikan pesan kesalahan
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while reading the file: " + e.getMessage());
        } catch (Exception e) {
            // Log error dan kembalikan pesan kesalahan
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }

    // Kelas respons untuk hasil klasifikasi
    public static class WasteResponse {
        private String classification; // Jenis sampah yang diklasifikasikan
        private double confidence; // Nilai kepercayaan klasifikasi

        // Constructor untuk membangun objek respons
        public WasteResponse(String classification, double confidence) {
            this.classification = classification;
            this.confidence = confidence;
        }

        // Getter untuk classification
        public String getClassification() {
            return classification;
        }

        // Getter untuk confidence
        public double getConfidence() {
            return confidence;
        }
    }
}
