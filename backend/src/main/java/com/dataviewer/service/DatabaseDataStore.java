package com.dataviewer.service;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import com.dataviewer.util.FilterSqlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("databaseDataStore")
@RequiredArgsConstructor
public class DatabaseDataStore implements DataStore {

    private static final Map<String, String> FIELD_COL = Map.ofEntries(
        Map.entry("roofvogelName",                 "rv.name"),
        Map.entry("roofvogelType",                 "rv.type"),
        Map.entry("roofvogelModelType",            "rv.model_type"),
        Map.entry("roofvogelModelTypeDescription", "rv.model_type_description"),
        Map.entry("roofvogelNumber",               "rv.number"),
        Map.entry("dierName",                      "d.name"),
        Map.entry("dierRole",                      "d.role"),
        Map.entry("dierType",                      "d.type"),
        Map.entry("dierTypeDescription",           "d.type_description"),
        Map.entry("dierTypeNumber",                "d.type_number"),
        Map.entry("functions",                     "d.functions"),
        Map.entry("services",                      "d.services"),
        Map.entry("kipIpAddress",                  "k.ip_address"),
        Map.entry("kipMacAddress",                 "k.mac_address"),
        Map.entry("kipType",                       "k.type"),
        Map.entry("kipSlangId",                    "k.roofvogel_slang_id"),
        Map.entry("slangId",                       "s.id"),
        Map.entry("slangDescription",              "s.description"),
        Map.entry("slangMask",                     "s.mask"),
        Map.entry("slangNetworkAddress",           "s.network_id_address"),
        Map.entry("slangType",                     "s.type")
    );

    private static final String BASE_FROM = """
        FROM kip k
        JOIN dier d ON k.dier_id = d.id
        JOIN roofvogel rv ON d.roofvogel_id = rv.id
        LEFT JOIN slang s ON s.id = k.roofvogel_slang_id AND s.roofvogel_id = rv.id
        """;

    private final JdbcTemplate jdbc;
    private boolean loaded = false;

    @Override
    @Transactional
    public void load(List<FlatRow> rows) {
        clear();
        log.info("Laden in database: {} rijen", rows.size());
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
        log.info("Database geladen");
    }

    @Override
    @Transactional
    public void clear() {
        log.debug("Database wissen");
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
        String where   = FilterSqlHelper.buildWhere(req.getFilters(), FIELD_COL, params);
        String orderBy = FilterSqlHelper.buildOrderBy(req.getSort(), FIELD_COL);

        long total = jdbc.queryForObject(
            "SELECT COUNT(*) " + BASE_FROM + where,
            Long.class, params.toArray()
        );
        log.debug("Query resultaat: {} rijen (pagina {}, grootte {})", total, req.getPage(), req.getSize());

        int pages = req.getSize() > 0 ? (int) Math.ceil((double) total / req.getSize()) : 1;

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(req.getSize());
        pageParams.add(req.getPage() * req.getSize());

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
            """ + BASE_FROM + where + orderBy + " LIMIT ? OFFSET ?";

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
}
