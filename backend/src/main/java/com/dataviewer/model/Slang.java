package com.dataviewer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class Slang {
    private String id;
    private String description;
    private String mask;
    private String networkIdAddress;
    private String type;
}
