import { createReducer, on } from '@ngrx/store';
import { FlatRow } from '../../models/flat-row.model';
import * as TableActions from './table.actions';

export interface FilterEntry { operator: string; value: string; }

export interface TableState {
  rows:          FlatRow[];
  totalElements: number;
  totalPages:    number;
  page:          number;
  pageSize:      number;
  filters:       Record<string, FilterEntry>;
  sort:          { field: string; direction: 'ASC' | 'DESC' } | null;
  loading:       boolean;
  error:         string | null;
}

export const initialState: TableState = {
  rows: [], totalElements: 0, totalPages: 0,
  page: 0, pageSize: 50,
  filters: {}, sort: null,
  loading: false, error: null,
};

export const tableReducer = createReducer(
  initialState,
  on(TableActions.setFilter, (s, { field, operator, value }) => ({
    ...s, filters: { ...s.filters, [field]: { operator, value } }, page: 0
  })),
  on(TableActions.clearFilter, (s, { field }) => {
    const { [field]: _, ...rest } = s.filters;
    return { ...s, filters: rest, page: 0 };
  }),
  on(TableActions.clearAllFilters, s => ({ ...s, filters: {}, page: 0 })),
  on(TableActions.setSort,      (s, { field, direction }) => ({ ...s, sort: { field, direction } })),
  on(TableActions.clearSort,    s => ({ ...s, sort: null })),
  on(TableActions.setPage,      (s, { page }) => ({ ...s, page })),
  on(TableActions.setPageSize,  (s, { size }) => ({ ...s, pageSize: size, page: 0 })),
  on(TableActions.loadData,     s => ({ ...s, loading: true, error: null })),
  on(TableActions.loadDataSuccess, (s, { content, totalElements, totalPages, page }) => ({
    ...s, rows: content, totalElements, totalPages, page, loading: false
  })),
  on(TableActions.loadDataFailure, (s, { error }) => ({ ...s, loading: false, error })),
);
