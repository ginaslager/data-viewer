import { createFeatureSelector, createSelector } from '@ngrx/store';
import { SettingsState } from './settings.reducer';
import { COLUMNS, GROUP_SPANS } from '../../models/column.model';

export const selectSettingsState    = createFeatureSelector<SettingsState>('settings');
export const selectColumnVisibility = createSelector(selectSettingsState, s => s.columnVisibility);
export const selectTheme            = createSelector(selectSettingsState, s => s.theme);
export const selectDensity          = createSelector(selectSettingsState, s => s.density);

export const selectVisibleColumns = createSelector(selectColumnVisibility, vis =>
  COLUMNS.filter(c => vis[c.field])
);

export const selectVisibleGroupSpans = createSelector(selectVisibleColumns, cols =>
  GROUP_SPANS
    .map(g => ({ ...g, span: cols.filter(c => c.group === g.group).length }))
    .filter(g => g.span > 0)
);
