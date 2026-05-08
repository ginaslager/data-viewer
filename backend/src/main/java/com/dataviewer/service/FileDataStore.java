package com.dataviewer.service;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import com.dataviewer.util.FilterSqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.*;
import java.util.*;

@Slf4j
@Service("fileDataStore")
public class FileDataStore implements DataStore {

    private static final Map<String, String> FIELD_COL = Map.ofEntries(
        Map.entry("roofvogelName",                "roofvogel_name"),
        Map.entry("roofvogelType",                "roofvogel_type"),
        Map.entry("roofvogelModelType",           "roofvogel_model_type"),
        Map.entry("roofvogelModelTypeDescription","roofvogel_model_type_desc"),
        Map.entry("roofvogelNumber",              "roofvogel_number"),
        Map.entry("dierName",                     "dier_name"),
        Map.entry("dierRole",                     "dier_role"),
        Map.entry("dierType",                     "dier_type"),
        Map.entry("dierTypeDescription",          "dier_type_description"),
        Map.entry("dierTypeNumber",               "dier_type_number"),
        Map.entry("dierVirtual",                  "dier_virtual"),
        Map.entry("functions",                    "functions"),
        Map.entry("services",                     "services"),
        Map.entry("kipIpAddress",                 "kip_ip_address"),
        Map.entry("kipMacAddress",                "kip_mac_address"),
        Map.entry("kipType",                      "kip_type"),
        Map.entry("kipSlangId",                   "kip_slang_id"),
        Map.entry("slangId",                      "slang_id"),
        Map.entry("slangDescription",             "slang_description"),
        Map.entry("slangMask",                    "slang_mask"),
        Map.entry("slangNetworkAddress",          "slang_network_address"),
        Map.entry("slangType",                    "slang_type")
    );

    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS flat_row (
            roofvogel_name                 TEXT,
            roofvogel_type                 TEXT,
            roofvogel_model_type           TEXT,
            roofvogel_model_type_desc      TEXT,
            roofvogel_number               TEXT,
            dier_name                      TEXT,
            dier_role                      TEXT,
            dier_type                      TEXT,
            dier_type_description          TEXT,
            dier_type_number               TEXT,
            dier_virtual                   TEXT,
            functions                      TEXT,
            services                       TEXT,
            kip_ip_address                 TEXT,
            kip_mac_address                TEXT,
            kip_type                       TEXT,
            kip_slang_id                   TEXT,
            slang_id                       TEXT,
            slang_description              TEXT,
            slang_mask                     TEXT,
            slang_network_address          TEXT,
            slang_type                     TEXT
        )
        """;

    private static final String INSERT = """
        INSERT INTO flat_row VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    private final String dbPath;
    private final int batchSize;

    public FileDataStore(
            @Value("${app.sqlite.path:./data/roofvogels.db}") String dbPath,
            @Value("${app.sqlite.batch-size:1000}") int batchSize) {
        this.dbPath    = dbPath;
        this.batchSize = batchSize;
    }

    @Override
    public void load(List<FlatRow> rows) {
        new File("./data").mkdirs();
        log.info("Laden in SQLite: {} rijen naar {}", rows.size(), dbPath);
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try (Statement st = con.createStatement()) {
                st.execute("DROP TABLE IF EXISTS flat_row");
                st.execute(CREATE_TABLE);
            }
            try (PreparedStatement ps = con.prepareStatement(INSERT)) {
                int batch = 0;
                for (FlatRow r : rows) {
                    ps.setString(1,  r.getRoofvogelName());
                    ps.setString(2,  r.getRoofvogelType());
                    ps.setString(3,  r.getRoofvogelModelType());
                    ps.setString(4,  r.getRoofvogelModelTypeDescription());
                    ps.setString(5,  r.getRoofvogelNumber());
                    ps.setString(6,  r.getDierName());
                    ps.setString(7,  r.getDierRole());
                    ps.setString(8,  r.getDierType());
                    ps.setString(9,  r.getDierTypeDescription());
                    ps.setString(10, r.getDierTypeNumber());
                    ps.setString(11, r.getDierVirtual() != null ? r.getDierVirtual().toString() : null);
                    ps.setString(12, r.getFunctions());
                    ps.setString(13, r.getServices());
                    ps.setString(14, r.getKipIpAddress());
                    ps.setString(15, r.getKipMacAddress());
                    ps.setString(16, r.getKipType());
                    ps.setString(17, r.getKipSlangId());
                    ps.setString(18, r.getSlangId());
                    ps.setString(19, r.getSlangDescription());
                    ps.setString(20, r.getSlangMask());
                    ps.setString(21, r.getSlangNetworkAddress());
                    ps.setString(22, r.getSlangType());
                    ps.addBatch();
                    if (++batch % batchSize == 0) ps.executeBatch();
                }
                ps.executeBatch();
            }
            con.commit();
            log.info("SQLite geladen");
        } catch (SQLException e) {
            throw new RuntimeException("SQLite load failed", e);
        }
    }

    @Override
    public void clear() {
        File db = new File(dbPath);
        if (!db.exists()) return;
        log.debug("SQLite wissen: {}", dbPath);
        try (Connection con = connect(); Statement st = con.createStatement()) {
            st.execute("DROP TABLE IF EXISTS flat_row");
        } catch (SQLException e) {
            throw new RuntimeException("SQLite clear failed", e);
        }
        db.delete();
    }

    @Override
    public boolean hasData() {
        if (!new File(dbPath).exists()) return false;
        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM flat_row")) {
            return rs.next() && rs.getLong(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public PageResult<FlatRow> query(DataRequest req) {
        List<Object> whereParams = new ArrayList<>();
        String where = FilterSqlHelper.buildWhere(req.getFilters(), FIELD_COL, whereParams);
        String order = FilterSqlHelper.buildOrderBy(req.getSort(), FIELD_COL);
        int size   = req.getSize() > 0 ? req.getSize() : 50;
        int offset = req.getPage() * size;

        String countSql = "SELECT COUNT(*) FROM flat_row" + where;
        String dataSql  = "SELECT * FROM flat_row" + where + order + " LIMIT ? OFFSET ?";

        List<Object> dataParams = new ArrayList<>(whereParams);
        dataParams.add(size);
        dataParams.add(offset);

        try (Connection con = connect()) {
            long total;
            try (PreparedStatement ps = con.prepareStatement(countSql)) {
                bind(ps, whereParams);
                ResultSet rs = ps.executeQuery();
                total = rs.next() ? rs.getLong(1) : 0;
            }
            log.debug("SQLite query: {} rijen (pagina {}, grootte {})", total, req.getPage(), size);
            List<FlatRow> page = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(dataSql)) {
                bind(ps, dataParams);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) page.add(map(rs));
            }
            int pages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
            return new PageResult<>(page, (int) total, pages, req.getPage());
        } catch (SQLException e) {
            throw new RuntimeException("SQLite query failed", e);
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    private FlatRow map(ResultSet rs) throws SQLException {
        String dv = rs.getString("dier_virtual");
        Boolean dierVirtual = dv != null ? Boolean.parseBoolean(dv) : null;
        return FlatRow.builder()
            .roofvogelName(rs.getString("roofvogel_name"))
            .roofvogelType(rs.getString("roofvogel_type"))
            .roofvogelModelType(rs.getString("roofvogel_model_type"))
            .roofvogelModelTypeDescription(rs.getString("roofvogel_model_type_desc"))
            .roofvogelNumber(rs.getString("roofvogel_number"))
            .dierName(rs.getString("dier_name"))
            .dierRole(rs.getString("dier_role"))
            .dierType(rs.getString("dier_type"))
            .dierTypeDescription(rs.getString("dier_type_description"))
            .dierTypeNumber(rs.getString("dier_type_number"))
            .dierVirtual(dierVirtual)
            .functions(rs.getString("functions"))
            .services(rs.getString("services"))
            .kipIpAddress(rs.getString("kip_ip_address"))
            .kipMacAddress(rs.getString("kip_mac_address"))
            .kipType(rs.getString("kip_type"))
            .kipSlangId(rs.getString("kip_slang_id"))
            .slangId(rs.getString("slang_id"))
            .slangDescription(rs.getString("slang_description"))
            .slangMask(rs.getString("slang_mask"))
            .slangNetworkAddress(rs.getString("slang_network_address"))
            .slangType(rs.getString("slang_type"))
            .build();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}
