package com.dataviewer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
public class Roofvogel {
    private String name;
    private String type;
    private String modelType;
    private String modelTypeDescription;
    private String number;
    private Dier         dier;
    private List<Slang>  slangen = new ArrayList<>();
}
