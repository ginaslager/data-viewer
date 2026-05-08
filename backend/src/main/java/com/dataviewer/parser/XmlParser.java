package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import lombok.extern.slf4j.Slf4j;
import javax.xml.stream.*;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class XmlParser implements FileParser {

    @Override
    public List<FlatRow> parse(InputStream stream) throws Exception {
        log.info("XML parsing gestart");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        XMLStreamReader reader = factory.createXMLStreamReader(stream);

        List<FlatRow> result = new ArrayList<>();

        // Roofvogel-level fields
        String rvName = null, rvType = null, rvModelType = null, rvModelTypeDesc = null, rvNumber = null;
        // Dier-level fields
        String dName = null, dRole = null, dType = null, dTypeDesc = null, dTypeNum = null;
        Boolean dVirtual = null;
        String dFunctions = null, dServices = null;
        // Kip-level fields
        String kIp = null, kMac = null, kType = null, kSlangId = null;
        // Slang being built
        String sId = null, sDesc = null, sMask = null, sNet = null, sType = null;
        // Slangen collected for current roofvogel (needed for kip→slang match)
        Map<String, String[]> slangMap = new LinkedHashMap<>();

        String field = null;
        String ctx   = null; // "dier" | "kip" | "slang" | "roofvogel"

        while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
                field = null;
                switch (reader.getLocalName()) {
                    case "roofvogel" -> {
                        rvName = rvType = rvModelType = rvModelTypeDesc = rvNumber = null;
                        dName = dRole = dType = dTypeDesc = dTypeNum = null;
                        dVirtual = null; dFunctions = dServices = null;
                        kIp = kMac = kType = kSlangId = null;
                        slangMap = new LinkedHashMap<>();
                        ctx = "roofvogel";
                    }
                    case "dier"    -> { dName = dRole = dType = dTypeDesc = dTypeNum = null; dVirtual = null; dFunctions = dServices = null; ctx = "dier"; }
                    case "kip"     -> { kIp = kMac = kType = kSlangId = null; ctx = "kip"; }
                    case "slang"   -> { sId = sDesc = sMask = sNet = sType = null; ctx = "slang"; }
                    case "slangen" -> { /* container */ }
                    default        -> field = reader.getLocalName();
                }

            } else if (event == XMLStreamConstants.CHARACTERS) {
                String text = reader.getText().trim();
                if (text.isEmpty() || field == null) continue;

                switch (ctx == null ? "" : ctx) {
                    case "slang" -> {
                        switch (field) {
                            case "id"               -> sId   = text;
                            case "description"      -> sDesc = text;
                            case "mask"             -> sMask = text;
                            case "networkIdAddress" -> sNet  = text;
                            case "type"             -> sType = text;
                        }
                    }
                    case "kip" -> {
                        switch (field) {
                            case "ipAddress"        -> kIp     = text;
                            case "macAddress"       -> kMac    = text;
                            case "type"             -> kType   = text;
                            case "roofvogelSlangId" -> kSlangId = text;
                        }
                    }
                    case "dier" -> {
                        switch (field) {
                            case "name"            -> dName    = text;
                            case "role"            -> dRole    = text;
                            case "type"            -> dType    = text;
                            case "typeDescription" -> dTypeDesc = text;
                            case "typeNumber"      -> dTypeNum  = text;
                            case "virtual"         -> dVirtual  = Boolean.parseBoolean(text);
                            case "functions"       -> dFunctions = text;
                            case "services"        -> dServices  = text;
                        }
                    }
                    case "roofvogel" -> {
                        switch (field) {
                            case "name"                 -> rvName        = text;
                            case "type"                 -> rvType        = text;
                            case "modelType"            -> rvModelType   = text;
                            case "modelTypeDescription" -> rvModelTypeDesc = text;
                            case "number"               -> rvNumber      = text;
                        }
                    }
                }

            } else if (event == XMLStreamConstants.END_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "slang" -> {
                        if (sId != null) slangMap.put(sId, new String[]{ sId, sDesc, sMask, sNet, sType });
                        ctx = "roofvogel";
                    }
                    case "kip"  -> ctx = "dier";
                    case "dier" -> ctx = "roofvogel";
                    case "roofvogel" -> {
                        String[] s = (kSlangId != null) ? slangMap.get(kSlangId) : null;
                        result.add(FlatRow.builder()
                            .roofvogelName(rvName)
                            .roofvogelType(rvType)
                            .roofvogelModelType(rvModelType)
                            .roofvogelModelTypeDescription(rvModelTypeDesc)
                            .roofvogelNumber(rvNumber)
                            .dierName(dName)
                            .dierRole(dRole)
                            .dierType(dType)
                            .dierTypeDescription(dTypeDesc)
                            .dierTypeNumber(dTypeNum)
                            .dierVirtual(dVirtual)
                            .functions(dFunctions)
                            .services(dServices)
                            .kipIpAddress(kIp)
                            .kipMacAddress(kMac)
                            .kipType(kType)
                            .kipSlangId(kSlangId)
                            .slangId(s != null ? s[0] : null)
                            .slangDescription(s != null ? s[1] : null)
                            .slangMask(s != null ? s[2] : null)
                            .slangNetworkAddress(s != null ? s[3] : null)
                            .slangType(s != null ? s[4] : null)
                            .build());
                        ctx = null;
                    }
                }
                field = null;
            }
        }
        reader.close();
        log.info("XML parsing klaar: {} rijen", result.size());
        return result;
    }
}
