import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { ApiService } from '../../services/api.service';
import * as UploadActions from './upload.actions';
import * as TableActions from '../table/table.actions';
import { switchMap, map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable()
export class UploadEffects {
  private actions$ = inject(Actions);
  private api      = inject(ApiService);

  upload$ = createEffect(() => this.actions$.pipe(
    ofType(UploadActions.uploadFile),
    switchMap(({ file, mode }) =>
      this.api.upload(file, mode).pipe(
        map(event => event.result !== undefined
          ? UploadActions.uploadSuccess({
              roofvogels: event.result['roofvogels'] ?? 0,
              dieren:     event.result['dieren']     ?? 0,
              kippen:     event.result['kippen']     ?? 0,
              slangen:    event.result['slangen']    ?? 0,
            })
          : UploadActions.uploadProgress({ progress: event.progress })
        ),
        catchError(err => of(UploadActions.uploadFailure({ error: err.message ?? 'Upload failed' })))
      )
    )
  ));

  loadAfterUpload$ = createEffect(() => this.actions$.pipe(
    ofType(UploadActions.uploadSuccess),
    map(() => TableActions.loadData())
  ));
}
