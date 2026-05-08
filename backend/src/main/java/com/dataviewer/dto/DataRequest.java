package com.dataviewer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.util.List;

@Data
public class DataRequest {
    @Valid
    private List<FilterCriteria> filters;

    private SortCriteria sort;

    @Min(value = 0, message = "Paginanummer mag niet negatief zijn")
    private int page;

    @Min(value = 1,   message = "Paginagrootte moet minimaal 1 zijn")
    @Max(value = 1000, message = "Paginagrootte mag maximaal 1000 zijn")
    private int size = 50;
}
