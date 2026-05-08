package com.dataviewer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FlatRow {
    // Roofvogel
    private String roofvogelName;
    private String roofvogelType;
    private String roofvogelModelType;
    private String roofvogelModelTypeDescription;
    private String roofvogelNumber;
    // Dier
    private String  dierName;
    private String  dierRole;
    private String  dierType;
    private String  dierTypeDescription;
    private String  dierTypeNumber;
    private Boolean dierVirtual;
    // Functies
    private String functions;
    private String services;
    // Kip
    private String kipIpAddress;
    private String kipMacAddress;
    private String kipType;
    private String kipSlangId;
    // Slang (matched via kipSlangId → slang.id)
    private String slangId;
    private String slangDescription;
    private String slangMask;
    private String slangNetworkAddress;
    private String slangType;
}
