import { Component, OnInit, Output, EventEmitter, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { FlatRow } from '../../models/flat-row.model';
import { Column, GroupSpan, COLUMNS, OPERATORS } from '../../models/column.model';
import * as TableActions from '../../store/table/table.actions';
import {
  selectRows, selectLoading, selectTotalPages, selectPage,
  selectPageSize, selectSort, selectFilters, selectActiveFilterCount
} from '../../store/table/table.selectors';
import { selectFileName } from '../../store/upload/upload.selectors';
import { selectVisibleColumns, selectVisibleGroupSpans, selectDensity } from '../../store/settings/settings.selectors';
import { Density } from '../../store/settings/settings.actions';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss'
})
export class DataTableComponent implements OnInit {
  @Output() navigateTo = new EventEmitter<string>();

  readonly operators = OPERATORS;
  readonly pageSizes = [10, 25, 50, 100, 200];

  openOpMenu: string | null = null;

  opLabel(op: string): string {
    return this.operators.find(o => o.value === op)?.label ?? 'contains';
  }

  toggleOpMenu(field: string, event: MouseEvent) {
    event.stopPropagation();
    this.openOpMenu = this.openOpMenu === field ? null : field;
  }

  pickOp(field: string, operator: string, event: MouseEvent) {
    event.stopPropagation();
    this.ctrl(`${field}_op`).setValue(operator);
    this.openOpMenu = null;
  }

  @HostListener('document:click')
  closeOpMenus() { this.openOpMenu = null; }

  columns$:           Observable<Column[]>;
  groupSpans$:        Observable<GroupSpan[]>;
  density$:           Observable<Density>;
  rows$:              Observable<FlatRow[]>;
  loading$:           Observable<boolean>;
  totalPages$:        Observable<number>;
  page$:              Observable<number>;
  pageSize$:          Observable<number>;
  sort$:              Observable<{ field: string; direction: 'ASC' | 'DESC' } | null>;
  activeFilterCount$: Observable<number>;
  fileName$:          Observable<string | null>;

  filterForm!: FormGroup;

  constructor(private store: Store, private fb: FormBuilder) {
    this.columns$           = store.select(selectVisibleColumns);
    this.groupSpans$        = store.select(selectVisibleGroupSpans);
    this.density$           = store.select(selectDensity);
    this.rows$              = store.select(selectRows);
    this.loading$           = store.select(selectLoading);
    this.totalPages$        = store.select(selectTotalPages);
    this.page$              = store.select(selectPage);
    this.pageSize$          = store.select(selectPageSize);
    this.sort$              = store.select(selectSort);
    this.activeFilterCount$ = store.select(selectActiveFilterCount);
    this.fileName$          = store.select(selectFileName);
  }

  ngOnInit() {
    const controls: Record<string, FormControl> = {};
    COLUMNS.forEach(c => {
      controls[`${c.field}_op`]  = this.fb.nonNullable.control('contains');
      controls[`${c.field}_val`] = this.fb.nonNullable.control('');
    });
    this.filterForm = this.fb.group(controls);

    COLUMNS.forEach(c => {
      this.ctrl(`${c.field}_val`).valueChanges.subscribe(value => {
        const op = this.ctrl(`${c.field}_op`).value;
        if (value.trim()) {
          this.store.dispatch(TableActions.setFilter({ field: c.field, operator: op, value }));
        } else {
          this.store.dispatch(TableActions.clearFilter({ field: c.field }));
        }
      });
      this.ctrl(`${c.field}_op`).valueChanges.subscribe(operator => {
        const value = this.ctrl(`${c.field}_val`).value;
        if (value.trim()) {
          this.store.dispatch(TableActions.setFilter({ field: c.field, operator, value }));
        }
      });
    });
  }

  ctrl(name: string): FormControl {
    return this.filterForm.get(name) as FormControl;
  }

  clearColumn(field: string) {
    this.filterForm.patchValue({ [`${field}_val`]: '', [`${field}_op`]: 'contains' });
    this.store.dispatch(TableActions.clearFilter({ field }));
  }

  clearAll() {
    const reset: Record<string, string> = {};
    COLUMNS.forEach(c => { reset[`${c.field}_val`] = ''; reset[`${c.field}_op`] = 'contains'; });
    this.filterForm.setValue(reset);
    this.store.dispatch(TableActions.clearAllFilters());
  }

  toggleSort(field: string, sortable: boolean, currentSort: { field: string; direction: 'ASC' | 'DESC' } | null) {
    if (!sortable) return;
    if (currentSort?.field === field) {
      const next = currentSort.direction === 'ASC' ? 'DESC' : 'ASC';
      this.store.dispatch(TableActions.setSort({ field, direction: next }));
    } else {
      this.store.dispatch(TableActions.setSort({ field, direction: 'ASC' }));
    }
  }

  goToPage(page: number) { this.store.dispatch(TableActions.setPage({ page })); }

  setPageSizeVal(size: number) { this.store.dispatch(TableActions.setPageSize({ size })); }

  jumpToPage(val: string, total: number) {
    const page = parseInt(val) - 1;
    if (!isNaN(page) && page >= 0 && page < total) {
      this.store.dispatch(TableActions.setPage({ page }));
    }
  }

  pages(total: number): number[] {
    return Array.from({ length: total }, (_, i) => i);
  }

  cellValue(row: FlatRow, field: string): string | number {
    const v = (row as unknown as Record<string, unknown>)[field];
    if (v === null || v === undefined) return '—';
    if (typeof v === 'boolean') return v ? '✓' : '—';
    return v as string | number;
  }

  showPage(p: number, current: number, total: number): boolean {
    return p < 2 || p > total - 3 || Math.abs(p - current) <= 1;
  }

  isEllipsis(p: number, current: number, total: number): boolean {
    return (p === 2 && current > 4) || (p === total - 3 && current < total - 5);
  }
}
