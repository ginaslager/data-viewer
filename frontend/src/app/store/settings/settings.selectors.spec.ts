import { COLUMNS } from '../../models/column.model';

const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, val: string) => { store[key] = val; },
    clear: () => { store = {}; },
  };
})();
Object.defineProperty(global, 'localStorage', { value: localStorageMock, configurable: true });

import { initialState, SettingsState } from './settings.reducer';
import {
  selectColumnVisibility, selectTheme, selectDensity,
  selectVisibleColumns, selectVisibleGroupSpans
} from './settings.selectors';

function stateWith(overrides: Partial<SettingsState>): { settings: SettingsState } {
  return { settings: { ...initialState, ...overrides } };
}

describe('settings selectors', () => {
  it('selectTheme retourneert het huidige thema', () => {
    expect(selectTheme(stateWith({ theme: 'ivory' }))).toBe('ivory');
  });

  it('selectDensity retourneert de huidige dichtheid', () => {
    expect(selectDensity(stateWith({ density: 'compact' }))).toBe('compact');
  });

  it('selectColumnVisibility retourneert de zichtbaarheidsmap', () => {
    const vis = { roofvogelName: true, dierName: false };
    expect(selectColumnVisibility(stateWith({ columnVisibility: vis }))).toEqual(vis);
  });

  describe('selectVisibleColumns', () => {
    it('retourneert alleen zichtbare kolommen', () => {
      const allVisible = COLUMNS.reduce((acc, c) => ({ ...acc, [c.field]: true }), {} as Record<string, boolean>);
      const result = selectVisibleColumns(stateWith({ columnVisibility: allVisible }));
      expect(result.length).toBe(COLUMNS.length);
    });

    it('filtert onzichtbare kolommen eruit', () => {
      const allHidden = COLUMNS.reduce((acc, c) => ({ ...acc, [c.field]: false }), {} as Record<string, boolean>);
      const result = selectVisibleColumns(stateWith({ columnVisibility: allHidden }));
      expect(result).toHaveLength(0);
    });

    it('retourneert alleen de zichtbare subset', () => {
      const vis = COLUMNS.reduce((acc, c, i) => ({ ...acc, [c.field]: i === 0 }), {} as Record<string, boolean>);
      const result = selectVisibleColumns(stateWith({ columnVisibility: vis }));
      expect(result).toHaveLength(1);
      expect(result[0].field).toBe(COLUMNS[0].field);
    });
  });

  describe('selectVisibleGroupSpans', () => {
    it('retourneert geen lege groepen', () => {
      const allHidden = COLUMNS.reduce((acc, c) => ({ ...acc, [c.field]: false }), {} as Record<string, boolean>);
      const result = selectVisibleGroupSpans(stateWith({ columnVisibility: allHidden }));
      expect(result.every(g => g.span > 0)).toBe(true);
    });

    it('telt zichtbare kolommen per groep', () => {
      const allVisible = COLUMNS.reduce((acc, c) => ({ ...acc, [c.field]: true }), {} as Record<string, boolean>);
      const result = selectVisibleGroupSpans(stateWith({ columnVisibility: allVisible }));
      const totalSpan = result.reduce((sum, g) => sum + g.span, 0);
      expect(totalSpan).toBe(COLUMNS.length);
    });
  });
});
