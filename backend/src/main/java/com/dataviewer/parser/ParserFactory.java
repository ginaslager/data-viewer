package com.dataviewer.parser;

import org.springframework.stereotype.Component;

@Component
public class ParserFactory {

    public FileParser forFilename(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xml"))  return new XmlParser();
        if (lower.endsWith(".json")) return new JsonParser();
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) return new YamlParser();
        if (lower.endsWith(".csv"))  return new CsvParser();
        throw new IllegalArgumentException("Unsupported file type: " + filename);
    }
}
