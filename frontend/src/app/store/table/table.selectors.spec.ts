import { initialState, TableState } from './table.reducer';
import {
  selectRows, selectTotalElements, selectPage, selectPageSize,
  selectLoading, selectActiveFilterCount, selectQueryParams
} from './table.selectors';

function stateWith(overrides: Partial<TableState>): { table: TableState } {
  return { table: { ...initialState, ...overrides } };
}

describe('table selectors', () => {
  it('selectRows retourneert rijen', () => {
    const row = { roofvogelName: 'Havik' } as any;
    expect(selectRows(stateWith({ rows: [row] }))).toEqual([row]);
  });

  it('selectTotalElements retourneert totaal', () => {
    expect(selectTotalElements(stateWith({ totalElements: 42 }))).toBe(42);
  });

  it('selectPage retourneert huidig paginanummer', () => {
    expect(selectPage(stateWith({ page: 3 }))).toBe(3);
  });

  it('selectPageSize retourneert paginagrootte', () => {
    expect(selectPageSize(stateWith({ pageSize: 100 }))).toBe(100);
  });

  it('selectLoading retourneert laadstatus', () => {
    expect(selectLoading(stateWith({ loading: true }))).toBe(true);
  });

  describe('selectActiveFilterCount', () => {
    it('telt alleen filters met een niet-lege waarde', () => {
      const filters = {
        roofvogelName: { operator: 'contains', value: 'Havik' },
        dierName:      { operator: 'contains', value: '' },
        kipType:       { operator: 'equals',   value: '  ' },
      };
      expect(selectActiveFilterCount(stateWith({ filters }))).toBe(1);
    });

    it('retourneert 0 als er geen filters zijn', () => {
      expect(selectActiveFilterCount(stateWith({ filters: {} }))).toBe(0);
    });
  });

  describe('selectQueryParams', () => {
    it('bouwt queryparameters zonder lege filters', () => {
      const filters = {
        roofvogelName: { operator: 'contains', value: 'Havik' },
        dierName:      { operator: 'contains', value: '' },
      };
      const params = selectQueryParams(stateWith({ filters, page: 2, pageSize: 25 }));
      expect(params.filters).toHaveLength(1);
      expect(params.filters[0]).toEqual({ field: 'roofvogelName', operator: 'contains', value: 'Havik' });
      expect(params.page).toBe(2);
      expect(params.size).toBe(25);
    });

    it('neemt sortering mee in queryparameters', () => {
      const sort = { field: 'roofvogelName', direction: 'DESC' as const };
      const params = selectQueryParams(stateWith({ sort }));
      expect(params.sort).toEqual(sort);
    });
  });
});
