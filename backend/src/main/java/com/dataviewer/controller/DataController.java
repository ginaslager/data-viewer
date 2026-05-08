package com.dataviewer.controller;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import com.dataviewer.service.DatabaseDataStore;
import com.dataviewer.service.FileDataStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final FileDataStore fileStore;
    private final DatabaseDataStore dbStore;

    @PostMapping
    public ResponseEntity<PageResult<FlatRow>> query(@Valid @RequestBody DataRequest request) {
        log.debug("Query aanvraag: pagina={}, grootte={}, filters={}", request.getPage(), request.getSize(), request.getFilters());
        if (dbStore.hasData()) {
            return ResponseEntity.ok(dbStore.query(request));
        }
        if (fileStore.hasData()) {
            return ResponseEntity.ok(fileStore.query(request));
        }
        return ResponseEntity.ok(new PageResult<>(List.of(), 0, 0, 0));
    }
}
