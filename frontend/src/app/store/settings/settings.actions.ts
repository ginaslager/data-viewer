import { createAction, props } from '@ngrx/store';

export type Theme   = 'carbon' | 'ivory' | 'slate';
export type Density = 'compact' | 'normal' | 'spacious';

export const toggleColumn  = createAction('[Settings] Toggle Column',  props<{ field: string }>());
export const resetColumns  = createAction('[Settings] Reset Columns');
export const setTheme      = createAction('[Settings] Set Theme',      props<{ theme: Theme }>());
export const setDensity    = createAction('[Settings] Set Density',    props<{ density: Density }>());
