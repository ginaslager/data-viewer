import { uploadReducer, initialState } from './upload.reducer';
import * as UploadActions from './upload.actions';

describe('uploadReducer', () => {
  it('retourneert de beginstatus bij onbekende actie', () => {
    expect(uploadReducer(undefined, { type: '@@UNKNOWN' })).toEqual(initialState);
  });

  describe('setMode', () => {
    it('stelt de modus in op database', () => {
      const state = uploadReducer(initialState, UploadActions.setMode({ mode: 'database' }));
      expect(state.mode).toBe('database');
    });

    it('stelt de modus in op file', () => {
      const withDb = uploadReducer(initialState, UploadActions.setMode({ mode: 'database' }));
      const state = uploadReducer(withDb, UploadActions.setMode({ mode: 'file' }));
      expect(state.mode).toBe('file');
    });
  });

  describe('uploadFile', () => {
    it('zet status op uploading en reset fout', () => {
      const withError = { ...initialState, error: 'Vorige fout', status: 'error' as const };
      const state = uploadReducer(withError, UploadActions.uploadFile({
        file: new File([], 'test.xml'),
        mode: 'file',
        fileName: 'test.xml',
      }));
      expect(state.status).toBe('uploading');
      expect(state.progress).toBe(0);
      expect(state.error).toBeNull();
      expect(state.fileName).toBe('test.xml');
    });
  });

  describe('uploadProgress', () => {
    it('werkt voortgang bij', () => {
      const state = uploadReducer(initialState, UploadActions.uploadProgress({ progress: 60 }));
      expect(state.progress).toBe(60);
    });
  });

  describe('uploadSuccess', () => {
    it('zet status op success met aantallen', () => {
      const state = uploadReducer(initialState, UploadActions.uploadSuccess({
        roofvogels: 52, dieren: 46, kippen: 37, slangen: 31
      }));
      expect(state.status).toBe('success');
      expect(state.progress).toBe(100);
      expect(state.hasData).toBe(true);
      expect(state.metadata?.counts?.roofvogels).toBe(52);
    });

    it('behoudt bestaande metadata en werkt alleen counts bij', () => {
      const withMeta = {
        ...initialState,
        metadata: {
          filename: 'test.xml',
          createDate: '2026-01-01',
          gitSha: 'abc123',
          description: 'Test',
          counts: null,
        },
      };
      const state = uploadReducer(withMeta, UploadActions.uploadSuccess({
        roofvogels: 10, dieren: 8, kippen: 5, slangen: 3
      }));
      expect(state.metadata?.filename).toBe('test.xml');
      expect(state.metadata?.gitSha).toBe('abc123');
      expect(state.metadata?.counts?.roofvogels).toBe(10);
    });
  });

  describe('uploadFailure', () => {
    it('zet status op error met foutmelding', () => {
      const state = uploadReducer(initialState, UploadActions.uploadFailure({ error: 'Netwerk fout' }));
      expect(state.status).toBe('error');
      expect(state.error).toBe('Netwerk fout');
    });
  });

  describe('resetUpload', () => {
    it('reset status en voortgang zonder hasData te wissen', () => {
      const withData = { ...initialState, status: 'success' as const, progress: 100, hasData: true };
      const state = uploadReducer(withData, UploadActions.resetUpload());
      expect(state.status).toBe('idle');
      expect(state.progress).toBe(0);
      expect(state.error).toBeNull();
      expect(state.hasData).toBe(true);
    });
  });

  describe('setMetadata', () => {
    it('slaat metadata op', () => {
      const metadata = {
        filename: 'roofvogels.xml',
        createDate: '2026-01-15',
        gitSha: 'deadbeef',
        description: 'Productie export',
        counts: null,
      };
      const state = uploadReducer(initialState, UploadActions.setMetadata({ metadata }));
      expect(state.metadata).toEqual(metadata);
    });
  });
});
