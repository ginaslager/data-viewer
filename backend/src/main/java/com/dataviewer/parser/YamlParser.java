package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.List;

public class YamlParser implements FileParser {

    private final JsonParser delegate = new JsonParser();
    private final ObjectMapper yaml   = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper json   = new ObjectMapper();

    @Override
    public List<FlatRow> parse(InputStream stream) throws Exception {
        JsonNode node     = yaml.readTree(stream);
        byte[]   jsonBytes = json.writeValueAsBytes(node);
        return delegate.parse(new java.io.ByteArrayInputStream(jsonBytes));
    }
}
