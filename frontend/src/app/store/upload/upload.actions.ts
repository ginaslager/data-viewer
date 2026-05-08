import { createAction, props } from '@ngrx/store';

export interface FileMetadata {
  filename:    string;
  createDate:  string | null;
  gitSha:      string | null;
  description: string | null;
  counts: {
    roofvogels: number;
    dieren:     number;
    kippen:     number;
    slangen:    number;
  } | null;
}

export const setMode        = createAction('[Upload] Set Mode',      props<{ mode: 'file' | 'database' }>());
export const uploadFile     = createAction('[Upload] Upload File',   props<{ file: File; mode: 'file' | 'database'; fileName: string }>());
export const uploadProgress = createAction('[Upload] Progress',      props<{ progress: number }>());
export const uploadSuccess  = createAction('[Upload] Success', props<{ roofvogels: number; dieren: number; kippen: number; slangen: number }>());
export const uploadFailure  = createAction('[Upload] Failure',       props<{ error: string }>());
export const resetUpload    = createAction('[Upload] Reset');
export const setMetadata    = createAction('[Upload] Set Metadata',  props<{ metadata: FileMetadata }>());
