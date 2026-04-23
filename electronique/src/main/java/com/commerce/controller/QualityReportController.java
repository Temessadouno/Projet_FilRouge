package com.commerce.controller;

import com.commerce.service.QualityReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Controller
@RequestMapping("/admin/rapport")
@RequiredArgsConstructor
public class QualityReportController {

    private final QualityReportService reportService;

    @GetMapping
    public String showReport(Model model) {
        try {
            var metrics = reportService.getMetrics();
            
            model.addAttribute("totalClasses", metrics.get("totalClasses"));
            model.addAttribute("totalInterfaces", metrics.get("totalInterfaces"));
            model.addAttribute("totalMethods", metrics.get("totalMethods"));
            model.addAttribute("totalLines", metrics.get("totalLines"));
            model.addAttribute("totalPackages", metrics.get("totalPackages"));
            model.addAttribute("totalTestMethods", metrics.get("totalTestMethods"));
            model.addAttribute("passedTests", metrics.get("passedTests"));
            model.addAttribute("failedTests", metrics.get("failedTests"));
            model.addAttribute("totalAssertions", metrics.get("totalAssertions"));
            model.addAttribute("testSuccessRate", metrics.get("testSuccessRate"));
            model.addAttribute("avgMethodsPerClass", metrics.get("avgMethodsPerClass"));
            model.addAttribute("avgLinesPerClass", metrics.get("avgLinesPerClass"));
            model.addAttribute("classMetrics", metrics.get("classMetrics"));
            model.addAttribute("testMetrics", metrics.get("testMetrics"));
            model.addAttribute("packageMetrics", metrics.get("packageMetrics"));
            model.addAttribute("reportDate", new Date());
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "admin/rapport";
    }
    
    @GetMapping("/presentation")
    public String showPresentation(Model model) {
        model.addAttribute("reportDate", new Date());
        return "admin/presentation";
    }

    @GetMapping("/download-word")
    public ResponseEntity<byte[]> downloadWordReport() {
        try {
            // Lire le template HTML depuis le fichier
            String content = readWordTemplate();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Rapport_Qualite_Logicielle.doc\"")
                    .contentType(MediaType.parseMediaType("application/msword"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private String readWordTemplate() throws Exception {
        // Lecture du fichier template depuis resources/templates/admin/rapport-word.html
        var inputStream = getClass().getClassLoader().getResourceAsStream("templates/admin/rapport-word.html");
        if (inputStream == null) {
            throw new Exception("Template non trouvé");
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}