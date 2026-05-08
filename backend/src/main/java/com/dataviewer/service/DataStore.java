package com.dataviewer.service;

import com.dataviewer.dto.DataRequest;
import com.dataviewer.dto.PageResult;
import com.dataviewer.model.FlatRow;
import java.util.List;

public interface DataStore {
    void load(List<FlatRow> rows);
    void clear();
    PageResult<FlatRow> query(DataRequest request);
    boolean hasData();
}
