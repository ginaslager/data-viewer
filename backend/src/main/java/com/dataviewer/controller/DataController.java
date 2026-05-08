package com.dataviewer.controller;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import com.dataviewer.service.DatabaseDataStore;
import com.dataviewer.service.FileDataStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final FileDataStore fileStore;
    private final DatabaseDataStore dbStore;

    public DataController(FileDataStore fileStore, DatabaseDataStore dbStore) {
        this.fileStore = fileStore;
        this.dbStore   = dbStore;
    }

    @PostMapping
    public ResponseEntity<PageResult<FlatRow>> query(@RequestBody DataRequest request) {
        if (dbStore.hasData()) {
            return ResponseEntity.ok(dbStore.query(request));
        }
        if (fileStore.hasData()) {
            return ResponseEntity.ok(fileStore.query(request));
        }
        return ResponseEntity.ok(new PageResult<>(java.util.List.of(), 0, 0, 0));
    }
}
