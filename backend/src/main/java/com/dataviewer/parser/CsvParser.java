package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import com.opencsv.CSVReader;
import java.io.*;
import java.util.*;

public class CsvParser implements FileParser {

    @Override
    public List<FlatRow> parse(InputStream stream) throws Exception {
        List<FlatRow> result = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(stream))) {
            String[] header = reader.readNext();
            if (header == null) return Collections.emptyList();
            Map<String, Integer> idx = buildIndex(header);
            String[] row;
            while ((row = reader.readNext()) != null) {
                String virtual = get(row, idx, "dierVirtual");
                String kipIp   = get(row, idx, "kipIpAddress");
                String slangId = get(row, idx, "slangId");
                result.add(FlatRow.builder()
                    .roofvogelName(get(row, idx, "roofvogelName"))
                    .roofvogelType(get(row, idx, "roofvogelType"))
                    .roofvogelModelType(get(row, idx, "roofvogelModelType"))
                    .roofvogelModelTypeDescription(get(row, idx, "roofvogelModelTypeDescription"))
                    .roofvogelNumber(get(row, idx, "roofvogelNumber"))
                    .dierName(get(row, idx, "dierName"))
                    .dierRole(get(row, idx, "dierRole"))
                    .dierType(get(row, idx, "dierType"))
                    .dierTypeDescription(get(row, idx, "dierTypeDescription"))
                    .dierTypeNumber(get(row, idx, "dierTypeNumber"))
                    .dierVirtual(virtual.isBlank() ? null : Boolean.parseBoolean(virtual))
                    .functions(get(row, idx, "functions"))
                    .services(get(row, idx, "services"))
                    .kipIpAddress(kipIp.isBlank() ? null : kipIp)
                    .kipMacAddress(get(row, idx, "kipMacAddress"))
                    .kipType(get(row, idx, "kipType"))
                    .kipSlangId(get(row, idx, "kipSlangId"))
                    .slangId(slangId.isBlank() ? null : slangId)
                    .slangDescription(get(row, idx, "slangDescription"))
                    .slangMask(get(row, idx, "slangMask"))
                    .slangNetworkAddress(get(row, idx, "slangNetworkAddress"))
                    .slangType(get(row, idx, "slangType"))
                    .build());
            }
        }
        return result;
    }

    private Map<String, Integer> buildIndex(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) map.put(header[i].trim(), i);
        return map;
    }

    private String get(String[] row, Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        return (i != null && i < row.length) ? row[i].trim() : "";
    }
}
