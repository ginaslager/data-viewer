package com.dataviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
}
