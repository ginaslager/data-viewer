package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XmlParserTest {

    private final XmlParser parser = new XmlParser();

    @Test
    void parseReturnsCorrectRowCount() throws Exception {
        List<FlatRow> rows = parseTestFile();
        assertThat(rows).hasSize(2);
    }

    @Test
    void parseExtractsRoofvogelFields() throws Exception {
        FlatRow first = parseTestFile().get(0);
        assertThat(first.getRoofvogelName()).isEqualTo("Havik");
        assertThat(first.getRoofvogelType()).isEqualTo("ROOFVOGEL");
        assertThat(first.getRoofvogelModelType()).isEqualTo("FALCO");
        assertThat(first.getRoofvogelNumber()).isEqualTo("RV-001");
    }

    @Test
    void parseExtractsDierFields() throws Exception {
        FlatRow first = parseTestFile().get(0);
        assertThat(first.getDierName()).isEqualTo("Feniks");
        assertThat(first.getDierRole()).isEqualTo("PRIMAIR");
        assertThat(first.getDierVirtual()).isFalse();
        assertThat(first.getFunctions()).isEqualTo("authenticatie,autorisatie");
    }

    @Test
    void parseExtractsKipFields() throws Exception {
        FlatRow first = parseTestFile().get(0);
        assertThat(first.getKipIpAddress()).isEqualTo("192.168.1.10");
        assertThat(first.getKipMacAddress()).isEqualTo("AA:BB:CC:DD:EE:01");
        assertThat(first.getKipSlangId()).isEqualTo("seg-intern-01");
    }

    @Test
    void parseExtractsSlangViaKipReference() throws Exception {
        FlatRow first = parseTestFile().get(0);
        assertThat(first.getSlangId()).isEqualTo("seg-intern-01");
        assertThat(first.getSlangDescription()).isEqualTo("Intern netwerk segment");
        assertThat(first.getSlangNetworkAddress()).isEqualTo("192.168.1.0");
    }

    @Test
    void parseHandlesRowWithoutKipOrSlang() throws Exception {
        FlatRow second = parseTestFile().get(1);
        assertThat(second.getKipIpAddress()).isNull();
        assertThat(second.getSlangId()).isNull();
        assertThat(second.getDierVirtual()).isTrue();
    }

    private List<FlatRow> parseTestFile() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test-roofvogels.xml")) {
            assertThat(is).isNotNull();
            return parser.parse(is);
        }
    }
}
