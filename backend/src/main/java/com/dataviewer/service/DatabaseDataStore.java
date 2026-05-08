package com.dataviewer.service;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.FilterCriteria;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("databaseDataStore")
public class DatabaseDataStore implements DataStore {

    private final JdbcTemplate jdbc;
    private boolean loaded = false;

    public DatabaseDataStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void load(List<FlatRow> rows) {
        clear();
        for (FlatRow r : rows) {
            jdbc.update("INSERT INTO roofvogel (name, type, model_type, model_type_description, number) VALUES (?, ?, ?, ?, ?)",
                r.getRoofvogelName(), r.getRoofvogelType(), r.getRoofvogelModelType(), r.getRoofvogelModelTypeDescription(), r.getRoofvogelNumber());
            long rvId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            jdbc.update("INSERT INTO dier (name, role, type, type_description, type_number, virtual, functions, services, roofvogel_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                r.getDierName(), r.getDierRole(), r.getDierType(), r.getDierTypeDescription(), r.getDierTypeNumber(),
                r.getDierVirtual(), r.getFunctions(), r.getServices(), rvId);
            long dierId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (r.getKipIpAddress() != null)
                jdbc.update("INSERT INTO kip (ip_address, mac_address, type, roofvogel_slang_id, dier_id) VALUES (?, ?, ?, ?, ?)",
                    r.getKipIpAddress(), r.getKipMacAddress(), r.getKipType(), r.getKipSlangId(), dierId);
        }
        loaded = true;
    }

    @Override
    public void clear() {
        jdbc.execute("DELETE FROM kip");
        jdbc.execute("DELETE FROM slang");
        jdbc.execute("DELETE FROM dier");
        jdbc.execute("DELETE FROM roofvogel");
        loaded = false;
    }

    @Override
    public boolean hasData() { return loaded; }

    @Override
    public PageResult<FlatRow> query(DataRequest req) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(req.getFilters(), params);
        String orderBy = buildOrderBy(req.getSort());

        String baseFrom = """
            FROM kip k
            JOIN dier d ON k.dier_id = d.id
            JOIN roofvogel rv ON d.roofvogel_id = rv.id
            LEFT JOIN slang s ON s.id = k.roofvogel_slang_id AND s.roofvogel_id = rv.id
            """;

        long total = jdbc.queryForObject(
            "SELECT COUNT(*) " + baseFrom + where,
            Long.class, params.toArray()
        );

        int pages = req.getSize() > 0 ? (int) Math.ceil((double) total / req.getSize()) : 1;
        int offset = req.getPage() * req.getSize();

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(req.getSize());
        pageParams.add(offset);

        String dataQuery = """
            SELECT rv.name AS roofvogelName, rv.type AS roofvogelType,
                   rv.model_type AS roofvogelModelType,
                   rv.model_type_description AS roofvogelModelTypeDescription,
                   rv.number AS roofvogelNumber,
                   d.name AS dierName, d.role AS dierRole, d.type AS dierType,
                   d.type_description AS dierTypeDescription, d.type_number AS dierTypeNumber,
                   d.virtual AS dierVirtual, d.functions AS functions, d.services AS services,
                   k.ip_address AS kipIpAddress, k.mac_address AS kipMacAddress,
                   k.type AS kipType, k.roofvogel_slang_id AS kipSlangId,
                   s.id AS slangId, s.description AS slangDescription,
                   s.mask AS slangMask, s.network_id_address AS slangNetworkAddress,
                   s.type AS slangType
            """ + baseFrom + where + orderBy + " LIMIT ? OFFSET ?";

        List<FlatRow> rows = jdbc.query(dataQuery, (rs, i) -> FlatRow.builder()
            .roofvogelName(rs.getString("roofvogelName"))
            .roofvogelType(rs.getString("roofvogelType"))
            .roofvogelModelType(rs.getString("roofvogelModelType"))
            .roofvogelModelTypeDescription(rs.getString("roofvogelModelTypeDescription"))
            .roofvogelNumber(rs.getString("roofvogelNumber"))
            .dierName(rs.getString("dierName"))
            .dierRole(rs.getString("dierRole"))
            .dierType(rs.getString("dierType"))
            .dierTypeDescription(rs.getString("dierTypeDescription"))
            .dierTypeNumber(rs.getString("dierTypeNumber"))
            .dierVirtual(rs.getObject("dierVirtual") != null ? rs.getBoolean("dierVirtual") : null)
            .functions(rs.getString("functions"))
            .services(rs.getString("services"))
            .kipIpAddress(rs.getString("kipIpAddress"))
            .kipMacAddress(rs.getString("kipMacAddress"))
            .kipType(rs.getString("kipType"))
            .kipSlangId(rs.getString("kipSlangId"))
            .slangId(rs.getString("slangId"))
            .slangDescription(rs.getString("slangDescription"))
            .slangMask(rs.getString("slangMask"))
            .slangNetworkAddress(rs.getString("slangNetworkAddress"))
            .slangType(rs.getString("slangType"))
            .build(), pageParams.toArray());

        return new PageResult<>(rows, total, pages, req.getPage());
    }

    private String buildWhere(List<FilterCriteria> filters, List<Object> params) {
        if (filters == null || filters.isEmpty()) return "";
        List<String> clauses = new ArrayList<>();
        for (FilterCriteria f : filters) {
            String col = toColumn(f.getField());
            if (col == null) continue;
            clauses.add(applyOp("LOWER(" + col + ")", f.getOperator()));
            params.add(toSqlValue(f.getOperator(), f.getValue().toLowerCase()));
        }
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    private String applyOp(String col, String op) {
        return switch (op) {
            case "contains", "startsWith", "endsWith" -> col + " LIKE ?";
            case "equals"    -> col + " = ?";
            case "notEquals" -> col + " != ?";
            default          -> col + " LIKE ?";
        };
    }

    private String toSqlValue(String op, String value) {
        return switch (op) {
            case "contains"   -> "%" + value + "%";
            case "startsWith" -> value + "%";
            case "endsWith"   -> "%" + value;
            default           -> value;
        };
    }

    private String toColumn(String field) {
        return switch (field) {
            case "roofvogelName"                 -> "rv.name";
            case "roofvogelType"                 -> "rv.type";
            case "roofvogelModelType"            -> "rv.model_type";
            case "roofvogelModelTypeDescription" -> "rv.model_type_description";
            case "roofvogelNumber"               -> "rv.number";
            case "dierName"                      -> "d.name";
            case "dierRole"                      -> "d.role";
            case "dierType"                      -> "d.type";
            case "dierTypeDescription"           -> "d.type_description";
            case "dierTypeNumber"                -> "d.type_number";
            case "functions"                     -> "d.functions";
            case "services"                      -> "d.services";
            case "kipIpAddress"                  -> "k.ip_address";
            case "kipMacAddress"                 -> "k.mac_address";
            case "kipType"                       -> "k.type";
            case "kipSlangId"                    -> "k.roofvogel_slang_id";
            case "slangId"                       -> "s.id";
            case "slangDescription"              -> "s.description";
            case "slangMask"                     -> "s.mask";
            case "slangNetworkAddress"           -> "s.network_id_address";
            case "slangType"                     -> "s.type";
            default                              -> null;
        };
    }

    private String buildOrderBy(com.dataviewer.dto.SortCriteria sort) {
        if (sort == null || sort.getField() == null) return "";
        String col = toColumn(sort.getField());
        if (col == null) return "";
        String dir = "DESC".equalsIgnoreCase(sort.getDirection()) ? "DESC" : "ASC";
        return " ORDER BY " + col + " " + dir;
    }
}
