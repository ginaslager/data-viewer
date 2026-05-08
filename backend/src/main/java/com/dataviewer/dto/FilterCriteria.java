package com.dataviewer.dto;

import lombok.Data;

@Data
public class FilterCriteria {
    private String field;
    private String operator; // contains, startsWith, endsWith, equals, notEquals
    private String value;
}
