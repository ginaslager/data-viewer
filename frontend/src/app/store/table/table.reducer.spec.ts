import { tableReducer, initialState } from './table.reducer';
import * as TableActions from './table.actions';

describe('tableReducer', () => {
  it('retourneert de beginstatus bij onbekende actie', () => {
    expect(tableReducer(undefined, { type: '@@UNKNOWN' })).toEqual(initialState);
  });

  describe('setFilter', () => {
    it('voegt een filter toe aan de lege filterstatus', () => {
      const state = tableReducer(initialState, TableActions.setFilter({
        field: 'roofvogelName', operator: 'contains', value: 'Havik'
      }));
      expect(state.filters['roofvogelName']).toEqual({ operator: 'contains', value: 'Havik' });
    });

    it('reset paginanummer naar 0 bij nieuw filter', () => {
      const withPage = { ...initialState, page: 3 };
      const state = tableReducer(withPage, TableActions.setFilter({
        field: 'dierName', operator: 'equals', value: 'Feniks'
      }));
      expect(state.page).toBe(0);
    });

    it('overschrijft bestaand filter voor hetzelfde veld', () => {
      const withFilter = tableReducer(initialState, TableActions.setFilter({
        field: 'roofvogelName', operator: 'contains', value: 'Oud'
      }));
      const state = tableReducer(withFilter, TableActions.setFilter({
        field: 'roofvogelName', operator: 'startsWith', value: 'Nieuw'
      }));
      expect(state.filters['roofvogelName']).toEqual({ operator: 'startsWith', value: 'Nieuw' });
    });
  });

  describe('clearFilter', () => {
    it('verwijdert een specifiek filter', () => {
      const withFilter = tableReducer(initialState, TableActions.setFilter({
        field: 'roofvogelName', operator: 'contains', value: 'Havik'
      }));
      const state = tableReducer(withFilter, TableActions.clearFilter({ field: 'roofvogelName' }));
      expect(state.filters['roofvogelName']).toBeUndefined();
    });

    it('laat andere filters intact', () => {
      let state = tableReducer(initialState, TableActions.setFilter({ field: 'roofvogelName', operator: 'contains', value: 'H' }));
      state = tableReducer(state, TableActions.setFilter({ field: 'dierName', operator: 'equals', value: 'F' }));
      state = tableReducer(state, TableActions.clearFilter({ field: 'roofvogelName' }));
      expect(state.filters['dierName']).toEqual({ operator: 'equals', value: 'F' });
    });
  });

  describe('clearAllFilters', () => {
    it('verwijdert alle filters', () => {
      let state = tableReducer(initialState, TableActions.setFilter({ field: 'roofvogelName', operator: 'contains', value: 'H' }));
      state = tableReducer(state, TableActions.setFilter({ field: 'dierName', operator: 'equals', value: 'F' }));
      state = tableReducer(state, TableActions.clearAllFilters());
      expect(Object.keys(state.filters)).toHaveLength(0);
    });
  });

  describe('setSort / clearSort', () => {
    it('stelt sortering in', () => {
      const state = tableReducer(initialState, TableActions.setSort({ field: 'roofvogelName', direction: 'ASC' }));
      expect(state.sort).toEqual({ field: 'roofvogelName', direction: 'ASC' });
    });

    it('wist sortering', () => {
      const withSort = tableReducer(initialState, TableActions.setSort({ field: 'roofvogelName', direction: 'ASC' }));
      const state = tableReducer(withSort, TableActions.clearSort());
      expect(state.sort).toBeNull();
    });
  });

  describe('setPage / setPageSize', () => {
    it('stelt paginanummer in', () => {
      const state = tableReducer(initialState, TableActions.setPage({ page: 5 }));
      expect(state.page).toBe(5);
    });

    it('reset paginanummer naar 0 bij nieuwe paginagrootte', () => {
      const withPage = tableReducer(initialState, TableActions.setPage({ page: 3 }));
      const state = tableReducer(withPage, TableActions.setPageSize({ size: 100 }));
      expect(state.pageSize).toBe(100);
      expect(state.page).toBe(0);
    });
  });

  describe('loadData / loadDataSuccess / loadDataFailure', () => {
    it('zet loading op true bij loadData', () => {
      const state = tableReducer(initialState, TableActions.loadData());
      expect(state.loading).toBe(true);
      expect(state.error).toBeNull();
    });

    it('slaat rijen op en zet loading op false bij succes', () => {
      const loading = tableReducer(initialState, TableActions.loadData());
      const state = tableReducer(loading, TableActions.loadDataSuccess({
        content: [{ roofvogelName: 'Havik' } as any],
        totalElements: 1,
        totalPages: 1,
        page: 0,
      }));
      expect(state.loading).toBe(false);
      expect(state.rows).toHaveLength(1);
      expect(state.totalElements).toBe(1);
    });

    it('slaat foutmelding op en zet loading op false bij fout', () => {
      const loading = tableReducer(initialState, TableActions.loadData());
      const state = tableReducer(loading, TableActions.loadDataFailure({ error: 'Serverfout' }));
      expect(state.loading).toBe(false);
      expect(state.error).toBe('Serverfout');
    });
  });
});
