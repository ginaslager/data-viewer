package com.dataviewer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class Dier {
    private String  name;
    private String  role;
    private String  type;
    private String  typeDescription;
    private String  typeNumber;
    private Boolean virtual;
    private String  functions;
    private String  services;
    private Kip     kip;
}
