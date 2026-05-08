import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { ApiService } from '../../services/api.service';
import * as TableActions from './table.actions';
import { selectQueryParams } from './table.selectors';
import { switchMap, map, catchError, withLatestFrom, debounceTime } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export class TableEffects {
  private actions$ = inject(Actions);
  private store    = inject(Store);
  private api      = inject(ApiService);

  triggerLoad$ = createEffect(() => this.actions$.pipe(
    ofType(
      TableActions.setFilter,
      TableActions.clearFilter,
      TableActions.clearAllFilters,
      TableActions.setSort,
      TableActions.clearSort,
      TableActions.setPageSize
    ),
    debounceTime(300),
    map(() => TableActions.setPage({ page: 0 }))
  ));

  triggerLoadOnPage$ = createEffect(() => this.actions$.pipe(
    ofType(TableActions.setPage),
    map(() => TableActions.loadData())
  ));

  loadData$ = createEffect(() => this.actions$.pipe(
    ofType(TableActions.loadData),
    withLatestFrom(this.store.select(selectQueryParams)),
    switchMap(([, params]) =>
      this.api.query(params).pipe(
        map(r => TableActions.loadDataSuccess({
          content:       r.content,
          totalElements: r.totalElements,
          totalPages:    r.totalPages,
          page:          r.page
        })),
        catchError(err => of(TableActions.loadDataFailure({ error: err.message })))
      )
    )
  ));
}
