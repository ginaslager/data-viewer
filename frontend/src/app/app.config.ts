import { ApplicationConfig } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { tableReducer } from './store/table/table.reducer';
import { uploadReducer } from './store/upload/upload.reducer';
import { settingsReducer } from './store/settings/settings.reducer';
import { TableEffects } from './store/table/table.effects';
import { UploadEffects } from './store/upload/upload.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(),
    provideStore({ table: tableReducer, upload: uploadReducer, settings: settingsReducer }),
    provideEffects([TableEffects, UploadEffects]),
    provideStoreDevtools({ maxAge: 25, logOnly: false }),
  ]
};
