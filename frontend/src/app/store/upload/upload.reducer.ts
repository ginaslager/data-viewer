import { createReducer, on } from '@ngrx/store';
import * as UploadActions from './upload.actions';
import { FileMetadata } from './upload.actions';

export interface UploadState {
  mode:     'file' | 'database' | null;
  status:   'idle' | 'uploading' | 'success' | 'error';
  progress: number;
  error:    string | null;
  hasData:  boolean;
  fileName: string | null;
  metadata: FileMetadata | null;
}

export const initialState: UploadState = {
  mode: 'file', status: 'idle', progress: 0, error: null, hasData: false, fileName: null, metadata: null
};

export const uploadReducer = createReducer(
  initialState,
  on(UploadActions.setMode,        (s, { mode })         => ({ ...s, mode })),
  on(UploadActions.uploadFile,     (s, { fileName })     => ({ ...s, status: 'uploading' as const, progress: 0, error: null, fileName })),
  on(UploadActions.uploadProgress, (s, { progress })     => ({ ...s, progress })),
  on(UploadActions.uploadSuccess,  (s, { roofvogels, dieren, kippen, slangen }) => ({
    ...s,
    status: 'success' as const,
    progress: 100,
    hasData: true,
    metadata: s.metadata
      ? { ...s.metadata, counts: { roofvogels, dieren, kippen, slangen } }
      : { filename: '', createDate: null, gitSha: null, description: null, counts: { roofvogels, dieren, kippen, slangen } }
  })),
  on(UploadActions.uploadFailure,  (s, { error })        => ({ ...s, status: 'error' as const, error })),
  on(UploadActions.resetUpload,    s                     => ({ ...s, status: 'idle' as const, progress: 0, error: null })),
  on(UploadActions.setMetadata,    (s, { metadata })     => ({ ...s, metadata })),
);
