package com.dataviewer.controller;

import com.dataviewer.model.FlatRow;
import com.dataviewer.parser.ParserFactory;
import com.dataviewer.service.DatabaseDataStore;
import com.dataviewer.service.FileDataStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final ParserFactory parserFactory;
    private final FileDataStore fileStore;
    private final DatabaseDataStore dbStore;

    public UploadController(ParserFactory parserFactory,
                            FileDataStore fileStore,
                            DatabaseDataStore dbStore) {
        this.parserFactory = parserFactory;
        this.fileStore     = fileStore;
        this.dbStore       = dbStore;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mode") String mode) {
        try {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.xml";
            List<FlatRow> rows = parserFactory.forFilename(filename).parse(file.getInputStream());

            fileStore.clear();
            dbStore.clear();

            if ("database".equalsIgnoreCase(mode)) {
                dbStore.load(rows);
            } else {
                fileStore.load(rows);
            }

            long countRoofvogels = rows.size();
            long countDieren     = rows.stream().filter(r -> r.getDierName()    != null).count();
            long countKippen     = rows.stream().filter(r -> r.getKipIpAddress() != null).count();
            long countSlangen    = rows.stream().filter(r -> r.getSlangId()      != null).count();

            return ResponseEntity.ok(Map.of(
                "status",     "ok",
                "mode",       mode,
                "roofvogels", countRoofvogels,
                "dieren",     countDieren,
                "kippen",     countKippen,
                "slangen",    countSlangen
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
