import { createFeatureSelector, createSelector } from '@ngrx/store';
import { UploadState } from './upload.reducer';

export const selectUploadState = createFeatureSelector<UploadState>('upload');

export const selectMode     = createSelector(selectUploadState, s => s.mode);
export const selectStatus   = createSelector(selectUploadState, s => s.status);
export const selectProgress = createSelector(selectUploadState, s => s.progress);
export const selectHasData  = createSelector(selectUploadState, s => s.hasData);
export const selectError    = createSelector(selectUploadState, s => s.error);
export const selectMetadata = createSelector(selectUploadState, s => s.metadata);
export const selectFileName = createSelector(selectUploadState, s => {
  if (!s.fileName) return null;
  const base = s.fileName.replace(/\.[^/.]+$/, '');
  return base.charAt(0).toUpperCase() + base.slice(1);
});
