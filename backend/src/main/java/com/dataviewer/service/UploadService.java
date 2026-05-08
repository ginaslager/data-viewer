package com.dataviewer.service;

import com.dataviewer.model.FlatRow;
import com.dataviewer.parser.ParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final ParserFactory parserFactory;
    private final FileDataStore fileStore;
    private final DatabaseDataStore dbStore;

    public Map<String, Object> upload(MultipartFile file, String mode) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.xml";
        log.info("Verwerken bestand: {} ({} bytes), modus: {}", filename, file.getSize(), mode);

        List<FlatRow> rows = parserFactory.forFilename(filename).parse(file.getInputStream());
        log.info("Geparseerd: {} rijen", rows.size());

        fileStore.clear();
        dbStore.clear();

        if ("database".equalsIgnoreCase(mode)) {
            dbStore.load(rows);
        } else {
            fileStore.load(rows);
        }

        Map<String, Object> result = Map.of(
            "status",     "ok",
            "mode",       mode,
            "roofvogels", (long) rows.size(),
            "dieren",     rows.stream().filter(r -> r.getDierName()     != null).count(),
            "kippen",     rows.stream().filter(r -> r.getKipIpAddress() != null).count(),
            "slangen",    rows.stream().filter(r -> r.getSlangId()      != null).count()
        );

        log.info("Upload voltooid: {}", result);
        return result;
    }
}
