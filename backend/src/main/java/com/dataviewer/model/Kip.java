package com.dataviewer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class Kip {
    private String ipAddress;
    private String macAddress;
    private String type;
    private String roofvogelSlangId;
}
