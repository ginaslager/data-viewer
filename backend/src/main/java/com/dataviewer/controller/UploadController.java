package com.dataviewer.controller;

import com.dataviewer.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mode") String mode) throws Exception {
        log.info("Upload aanvraag ontvangen: bestand={}, modus={}", file.getOriginalFilename(), mode);
        return ResponseEntity.ok(uploadService.upload(file, mode));
    }
}
