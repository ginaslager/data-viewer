package com.dataviewer.parser;

import com.dataviewer.model.FlatRow;
import java.io.InputStream;
import java.util.List;

public interface FileParser {
    List<FlatRow> parse(InputStream stream) throws Exception;
}
