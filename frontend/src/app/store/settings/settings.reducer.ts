import { createReducer, on } from '@ngrx/store';
import { COLUMNS } from '../../models/column.model';
import * as SettingsActions from './settings.actions';
import { Theme, Density } from './settings.actions';

export interface SettingsState {
  columnVisibility: Record<string, boolean>;
  theme: Theme;
  density: Density;
}

const defaultVisibility = COLUMNS.reduce((acc, col) => {
  acc[col.field] = col.visible;
  return acc;
}, {} as Record<string, boolean>);

const savedDensity = localStorage.getItem('density') as Density | null;

export const initialState: SettingsState = {
  columnVisibility: defaultVisibility,
  theme: 'carbon',
  density: savedDensity ?? 'normal',
};

export const settingsReducer = createReducer(
  initialState,
  on(SettingsActions.toggleColumn, (s, { field }) => ({
    ...s,
    columnVisibility: { ...s.columnVisibility, [field]: !s.columnVisibility[field] },
  })),
  on(SettingsActions.resetColumns, s => ({ ...s, columnVisibility: { ...defaultVisibility } })),
  on(SettingsActions.setTheme,     (s, { theme }) => ({ ...s, theme })),
  on(SettingsActions.setDensity,   (s, { density }) => ({ ...s, density })),
);
