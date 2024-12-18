package com.example.wasteclassification.controller;

import com.example.wasteclassification.service.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/classification")
public class ClassificationController {

    @Autowired
    private ClassificationService classificationService;

    @GetMapping
    public String showClassificationPage() {
        // Mengarahkan ke halaman classification.html
        return "classification";
    }

    @PostMapping
    public String classifyImage(@RequestParam("image") MultipartFile image, Model model) {
        try {
            // Memanggil service untuk klasifikasi
            String result = classificationService.classifyImage(image);
            
            if (result != null && !result.isEmpty()) {
                model.addAttribute("classification", result);
            } else {
                model.addAttribute("error", "Hasil klasifikasi tidak valid.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Terjadi kesalahan saat mengirim gambar: " + e.getMessage());
        }

        return "classification";
    }
}
