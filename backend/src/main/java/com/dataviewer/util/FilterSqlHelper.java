package com.dataviewer.util;

import com.dataviewer.dto.FilterCriteria;
import com.dataviewer.dto.SortCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FilterSqlHelper {

    private FilterSqlHelper() {}

    public static String buildWhere(List<FilterCriteria> filters, Map<String, String> fieldToColumn, List<Object> params) {
        if (filters == null || filters.isEmpty()) return "";
        List<String> clauses = new ArrayList<>();
        for (FilterCriteria f : filters) {
            String col = fieldToColumn.get(f.getField());
            if (col == null || f.getValue() == null || f.getValue().isBlank()) continue;
            clauses.add("LOWER(" + col + ") " + operatorSql(f.getOperator()));
            params.add(operatorParam(f.getOperator(), f.getValue().toLowerCase()));
        }
        return clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
    }

    public static String buildOrderBy(SortCriteria sort, Map<String, String> fieldToColumn) {
        if (sort == null || sort.getField() == null) return "";
        String col = fieldToColumn.get(sort.getField());
        if (col == null) return "";
        String dir = "DESC".equalsIgnoreCase(sort.getDirection()) ? "DESC" : "ASC";
        return " ORDER BY LOWER(" + col + ") " + dir;
    }

    private static String operatorSql(String op) {
        return switch (op) {
            case "equals"    -> "= ?";
            case "notEquals" -> "!= ?";
            default          -> "LIKE ?";
        };
    }

    private static String operatorParam(String op, String value) {
        return switch (op) {
            case "contains"   -> "%" + value + "%";
            case "startsWith" -> value + "%";
            case "endsWith"   -> "%" + value;
            default           -> value;
        };
    }
}
