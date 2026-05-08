package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

public class JsonParser implements FileParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<FlatRow> parse(InputStream stream) throws Exception {
        JsonNode root  = mapper.readTree(stream);
        JsonNode nodes = root.isArray() ? root : root.get("roofvogels");
        List<FlatRow> result = new ArrayList<>();
        for (JsonNode n : nodes) result.add(parseRow(n));
        return result;
    }

    private FlatRow parseRow(JsonNode rv) {
        JsonNode dierNode = rv.has("dier") && !rv.get("dier").isNull() ? rv.get("dier") : null;
        JsonNode kipNode  = dierNode != null && dierNode.has("kip") && !dierNode.get("kip").isNull() ? dierNode.get("kip") : null;

        Map<String, String[]> slangMap = new LinkedHashMap<>();
        for (JsonNode s : safe(rv, "slangen")) {
            String id = text(s, "id");
            if (id != null) slangMap.put(id, new String[]{
                id, text(s, "description"), text(s, "mask"), text(s, "networkIdAddress"), text(s, "type")
            });
        }

        String kipSlangId = kipNode != null ? text(kipNode, "roofvogelSlangId") : null;
        String[] s = kipSlangId != null ? slangMap.get(kipSlangId) : null;

        return FlatRow.builder()
            .roofvogelName(text(rv, "name"))
            .roofvogelType(text(rv, "type"))
            .roofvogelModelType(text(rv, "modelType"))
            .roofvogelModelTypeDescription(text(rv, "modelTypeDescription"))
            .roofvogelNumber(text(rv, "number"))
            .dierName(dierNode != null ? text(dierNode, "name") : null)
            .dierRole(dierNode != null ? text(dierNode, "role") : null)
            .dierType(dierNode != null ? text(dierNode, "type") : null)
            .dierTypeDescription(dierNode != null ? text(dierNode, "typeDescription") : null)
            .dierTypeNumber(dierNode != null ? text(dierNode, "typeNumber") : null)
            .dierVirtual(dierNode != null && dierNode.has("virtual") && !dierNode.get("virtual").isNull() ? dierNode.get("virtual").asBoolean() : null)
            .functions(dierNode != null ? text(dierNode, "functions") : null)
            .services(dierNode != null ? text(dierNode, "services") : null)
            .kipIpAddress(kipNode != null ? text(kipNode, "ipAddress") : null)
            .kipMacAddress(kipNode != null ? text(kipNode, "macAddress") : null)
            .kipType(kipNode != null ? text(kipNode, "type") : null)
            .kipSlangId(kipSlangId)
            .slangId(s != null ? s[0] : null)
            .slangDescription(s != null ? s[1] : null)
            .slangMask(s != null ? s[2] : null)
            .slangNetworkAddress(s != null ? s[3] : null)
            .slangType(s != null ? s[4] : null)
            .build();
    }

    private String text(JsonNode n, String field) {
        return n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }

    private Iterable<JsonNode> safe(JsonNode n, String field) {
        return n.has(field) ? n.get(field) : Collections.emptyList();
    }
}
