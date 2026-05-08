import { createAction, props } from '@ngrx/store';
import { FlatRow } from '../../models/flat-row.model';

export const setFilter = createAction(
  '[Table] Set Filter',
  props<{ field: string; operator: string; value: string }>()
);
export const clearFilter    = createAction('[Table] Clear Filter',     props<{ field: string }>());
export const clearAllFilters = createAction('[Table] Clear All Filters');
export const setSort        = createAction('[Table] Set Sort',         props<{ field: string; direction: 'ASC' | 'DESC' }>());
export const clearSort      = createAction('[Table] Clear Sort');
export const setPage        = createAction('[Table] Set Page',         props<{ page: number }>());
export const setPageSize    = createAction('[Table] Set Page Size',    props<{ size: number }>());

export const loadData        = createAction('[Table] Load Data');
export const loadDataSuccess = createAction('[Table] Load Data Success',
  props<{ content: FlatRow[]; totalElements: number; totalPages: number; page: number }>());
export const loadDataFailure = createAction('[Table] Load Data Failure', props<{ error: string }>());

export const restoreFromUrl = createAction('[Table] Restore From URL',
  props<{
    page:     number;
    pageSize: number;
    sort:     { field: string; direction: 'ASC' | 'DESC' } | null;
    filters:  Record<string, { operator: string; value: string }>;
  }>()
);
