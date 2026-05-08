package com.dataviewer.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParserFactory {

    public FileParser forFilename(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xml"))  { log.debug("XML parser geselecteerd voor {}", filename); return new XmlParser(); }
        if (lower.endsWith(".json")) { log.debug("JSON parser geselecteerd voor {}", filename); return new JsonParser(); }
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) { log.debug("YAML parser geselecteerd voor {}", filename); return new YamlParser(); }
        if (lower.endsWith(".csv"))  { log.debug("CSV parser geselecteerd voor {}", filename); return new CsvParser(); }
        throw new IllegalArgumentException("Niet-ondersteund bestandstype: " + filename);
    }
}
