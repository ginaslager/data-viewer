import { COLUMNS } from '../../models/column.model';

// Mock localStorage before importing the reducer (it reads localStorage at module load time)
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, val: string) => { store[key] = val; },
    clear: () => { store = {}; },
  };
})();
Object.defineProperty(global, 'localStorage', { value: localStorageMock });

import { settingsReducer, initialState } from './settings.reducer';
import * as SettingsActions from './settings.actions';

describe('settingsReducer', () => {
  beforeEach(() => localStorageMock.clear());

  it('retourneert de beginstatus bij onbekende actie', () => {
    const state = settingsReducer(undefined, { type: '@@UNKNOWN' });
    expect(state.theme).toBe('carbon');
    expect(state.density).toBe('normal');
    expect(state.columnVisibility).toBeDefined();
  });

  it('beginstatus heeft kolomzichtbaarheid van COLUMNS', () => {
    const state = settingsReducer(undefined, { type: '@@UNKNOWN' });
    const visibleCount = COLUMNS.filter(c => c.visible).length;
    const visibleInState = Object.values(state.columnVisibility).filter(Boolean).length;
    expect(visibleInState).toBe(visibleCount);
  });

  describe('toggleColumn', () => {
    it('schakelt een zichtbare kolom uit', () => {
      const visibleField = COLUMNS.find(c => c.visible)!.field;
      const state = settingsReducer(initialState, SettingsActions.toggleColumn({ field: visibleField }));
      expect(state.columnVisibility[visibleField]).toBe(false);
    });

    it('schakelt een verborgen kolom aan', () => {
      const hiddenField = COLUMNS.find(c => !c.visible)?.field;
      if (!hiddenField) return; // skip if all columns visible by default
      const state = settingsReducer(initialState, SettingsActions.toggleColumn({ field: hiddenField }));
      expect(state.columnVisibility[hiddenField]).toBe(true);
    });

    it('past alleen de betreffende kolom aan', () => {
      const fields = COLUMNS.slice(0, 3).map(c => c.field);
      const state = settingsReducer(initialState, SettingsActions.toggleColumn({ field: fields[0] }));
      expect(state.columnVisibility[fields[1]]).toBe(initialState.columnVisibility[fields[1]]);
      expect(state.columnVisibility[fields[2]]).toBe(initialState.columnVisibility[fields[2]]);
    });
  });

  describe('resetColumns', () => {
    it('herstelt kolomzichtbaarheid naar standaard', () => {
      const field = COLUMNS.find(c => c.visible)!.field;
      const modified = settingsReducer(initialState, SettingsActions.toggleColumn({ field }));
      const reset = settingsReducer(modified, SettingsActions.resetColumns());
      expect(reset.columnVisibility[field]).toBe(initialState.columnVisibility[field]);
    });
  });

  describe('setTheme', () => {
    it('stelt het thema in', () => {
      const state = settingsReducer(initialState, SettingsActions.setTheme({ theme: 'ivory' }));
      expect(state.theme).toBe('ivory');
    });

    it('past alleen het thema aan', () => {
      const state = settingsReducer(initialState, SettingsActions.setTheme({ theme: 'slate' }));
      expect(state.density).toBe(initialState.density);
    });
  });

  describe('setDensity', () => {
    it('stelt de dichtheid in', () => {
      const state = settingsReducer(initialState, SettingsActions.setDensity({ density: 'compact' }));
      expect(state.density).toBe('compact');
    });

    it('stelt spacious in', () => {
      const state = settingsReducer(initialState, SettingsActions.setDensity({ density: 'spacious' }));
      expect(state.density).toBe('spacious');
    });
  });
});
