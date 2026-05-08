import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TableState } from './table.reducer';

export const selectTableState = createFeatureSelector<TableState>('table');

export const selectRows          = createSelector(selectTableState, s => s.rows);
export const selectTotalElements = createSelector(selectTableState, s => s.totalElements);
export const selectTotalPages    = createSelector(selectTableState, s => s.totalPages);
export const selectPage          = createSelector(selectTableState, s => s.page);
export const selectPageSize      = createSelector(selectTableState, s => s.pageSize);
export const selectFilters       = createSelector(selectTableState, s => s.filters);
export const selectSort          = createSelector(selectTableState, s => s.sort);
export const selectLoading       = createSelector(selectTableState, s => s.loading);

export const selectActiveFilterCount = createSelector(selectFilters,
  filters => Object.values(filters).filter(f => f.value.trim()).length
);

export const selectQueryParams = createSelector(selectTableState, s => ({
  filters: Object.entries(s.filters)
    .filter(([, v]) => v.value.trim())
    .map(([field, v]) => ({ field, operator: v.operator, value: v.value })),
  sort: s.sort,
  page: s.page,
  size: s.pageSize,
}));
