package com.dataviewer.dto;

import lombok.Data;
import java.util.List;

@Data
public class DataRequest {
    private List<FilterCriteria> filters;
    private SortCriteria sort;
    private int page;
    private int size = 50;
}
