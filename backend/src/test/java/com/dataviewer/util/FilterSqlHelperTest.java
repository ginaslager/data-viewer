package com.dataviewer.util;

import com.dataviewer.dto.FilterCriteria;
import com.dataviewer.dto.SortCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FilterSqlHelperTest {

    private static final Map<String, String> COLS = Map.of(
        "name", "col_name",
        "type", "col_type"
    );

    @Test
    void buildWhereReturnsEmptyForNullFilters() {
        List<Object> params = new ArrayList<>();
        assertThat(FilterSqlHelper.buildWhere(null, COLS, params)).isEmpty();
        assertThat(params).isEmpty();
    }

    @Test
    void buildWhereReturnsEmptyForEmptyFilters() {
        List<Object> params = new ArrayList<>();
        assertThat(FilterSqlHelper.buildWhere(List.of(), COLS, params)).isEmpty();
    }

    @Test
    void buildWhereSkipsUnknownField() {
        List<Object> params = new ArrayList<>();
        FilterCriteria f = new FilterCriteria();
        f.setField("unknownField");
        f.setOperator("contains");
        f.setValue("test");
        String where = FilterSqlHelper.buildWhere(List.of(f), COLS, params);
        assertThat(where).isEmpty();
        assertThat(params).isEmpty();
    }

    @Test
    void buildWhereSkipsBlankValue() {
        List<Object> params = new ArrayList<>();
        FilterCriteria f = new FilterCriteria();
        f.setField("name");
        f.setOperator("contains");
        f.setValue("   ");
        String where = FilterSqlHelper.buildWhere(List.of(f), COLS, params);
        assertThat(where).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "contains,   test, %test%",
        "startsWith, test, test%",
        "endsWith,   test, %test",
        "equals,     test, test",
        "notEquals,  test, test"
    })
    void buildWhereProducesCorrectParam(String op, String value, String expectedParam) {
        List<Object> params = new ArrayList<>();
        FilterCriteria f = new FilterCriteria();
        f.setField("name");
        f.setOperator(op);
        f.setValue(value.trim());
        FilterSqlHelper.buildWhere(List.of(f), COLS, params);
        assertThat(params).containsExactly(expectedParam);
    }

    @Test
    void buildWhereContainsUsesLike() {
        List<Object> params = new ArrayList<>();
        FilterCriteria f = new FilterCriteria();
        f.setField("name");
        f.setOperator("contains");
        f.setValue("Havik");
        String where = FilterSqlHelper.buildWhere(List.of(f), COLS, params);
        assertThat(where).contains("LOWER(col_name) LIKE ?");
        assertThat(where).startsWith(" WHERE ");
    }

    @Test
    void buildWhereEqualsUsesEquals() {
        List<Object> params = new ArrayList<>();
        FilterCriteria f = new FilterCriteria();
        f.setField("name");
        f.setOperator("equals");
        f.setValue("Havik");
        String where = FilterSqlHelper.buildWhere(List.of(f), COLS, params);
        assertThat(where).contains("LOWER(col_name) = ?");
    }

    @Test
    void buildWhereMultipleFiltersJoinedWithAnd() {
        List<Object> params = new ArrayList<>();
        FilterCriteria f1 = new FilterCriteria();
        f1.setField("name"); f1.setOperator("contains"); f1.setValue("a");
        FilterCriteria f2 = new FilterCriteria();
        f2.setField("type"); f2.setOperator("equals"); f2.setValue("b");
        String where = FilterSqlHelper.buildWhere(List.of(f1, f2), COLS, params);
        assertThat(where).contains(" AND ");
        assertThat(params).hasSize(2);
    }

    @Test
    void buildOrderByReturnsEmptyForNullSort() {
        assertThat(FilterSqlHelper.buildOrderBy(null, COLS)).isEmpty();
    }

    @Test
    void buildOrderByReturnsEmptyForUnknownField() {
        SortCriteria s = new SortCriteria();
        s.setField("unknownField");
        s.setDirection("ASC");
        assertThat(FilterSqlHelper.buildOrderBy(s, COLS)).isEmpty();
    }

    @Test
    void buildOrderByDefaultsToAsc() {
        SortCriteria s = new SortCriteria();
        s.setField("name");
        s.setDirection(null);
        assertThat(FilterSqlHelper.buildOrderBy(s, COLS)).endsWith("ASC");
    }

    @Test
    void buildOrderByDescIsRespected() {
        SortCriteria s = new SortCriteria();
        s.setField("name");
        s.setDirection("DESC");
        String order = FilterSqlHelper.buildOrderBy(s, COLS);
        assertThat(order).contains("LOWER(col_name)").endsWith("DESC");
    }
}
