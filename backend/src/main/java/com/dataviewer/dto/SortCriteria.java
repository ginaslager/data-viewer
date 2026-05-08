package com.dataviewer.dto;

import lombok.Data;

@Data
public class SortCriteria {
    private String field;
    private String direction; // ASC, DESC
}
