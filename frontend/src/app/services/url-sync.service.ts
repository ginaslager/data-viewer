import { Injectable, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { Store } from '@ngrx/store';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { TableState } from '../store/table/table.reducer';
import { selectTableState } from '../store/table/table.selectors';
import * as TableActions from '../store/table/table.actions';

@Injectable({ providedIn: 'root' })
export class UrlSyncService {
  private static readonly FILTER_PREFIX = 'f_';
  private static readonly DEFAULT_SIZE  = 50;

  private readonly router     = inject(Router);
  private readonly route      = inject(ActivatedRoute);
  private readonly store      = inject(Store);
  private readonly destroyRef = inject(DestroyRef);

  init(): void {
    // Phase 1: URL → store (once, synchronously via snapshot)
    const restored = this.parseUrl(this.route.snapshot.queryParamMap);
    this.store.dispatch(TableActions.restoreFromUrl(restored));

    // Phase 2: store → URL (ongoing)
    this.store.select(selectTableState).pipe(
      map(state => this.buildQueryParams(state)),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(queryParams => {
      this.router.navigate([], { queryParams, replaceUrl: true });
    });
  }

  private parseUrl(params: ParamMap): {
    page:     number;
    pageSize: number;
    sort:     { field: string; direction: 'ASC' | 'DESC' } | null;
    filters:  Record<string, { operator: string; value: string }>;
  } {
    const page     = Math.max(0, parseInt(params.get('page') ?? '0', 10) || 0);
    const pageSize = parseInt(params.get('size') ?? '50', 10) || UrlSyncService.DEFAULT_SIZE;

    let sort: { field: string; direction: 'ASC' | 'DESC' } | null = null;
    const sortParam = params.get('sort');
    if (sortParam) {
      const idx = sortParam.lastIndexOf(':');
      if (idx > 0) {
        const field = sortParam.substring(0, idx);
        const dir   = sortParam.substring(idx + 1).toUpperCase();
        if (field && (dir === 'ASC' || dir === 'DESC')) {
          sort = { field, direction: dir as 'ASC' | 'DESC' };
        }
      }
    }

    const filters: Record<string, { operator: string; value: string }> = {};
    params.keys
      .filter((k: string) => k.startsWith(UrlSyncService.FILTER_PREFIX))
      .forEach((k: string) => {
        const field = k.substring(UrlSyncService.FILTER_PREFIX.length);
        const raw   = params.get(k) ?? '';
        const idx   = raw.indexOf(':');
        if (idx > 0) {
          const operator = raw.substring(0, idx);
          const value    = raw.substring(idx + 1);
          if (operator && value) filters[field] = { operator, value };
        }
      });

    return { page, pageSize, sort, filters };
  }

  private buildQueryParams(state: TableState): Record<string, string> {
    const p: Record<string, string> = {};

    if (state.page > 0)
      p['page'] = String(state.page);
    if (state.pageSize !== UrlSyncService.DEFAULT_SIZE)
      p['size'] = String(state.pageSize);
    if (state.sort)
      p['sort'] = `${state.sort.field}:${state.sort.direction}`;

    Object.entries(state.filters)
      .filter(([, v]) => v.value.trim())
      .forEach(([field, { operator, value }]) => {
        p[`${UrlSyncService.FILTER_PREFIX}${field}`] = `${operator}:${value}`;
      });

    return p;
  }
}
