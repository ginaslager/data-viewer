import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService, PageResult } from './api.service';
import { environment } from '../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService],
    });
    service = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('query()', () => {
    const mockResult: PageResult = {
      content: [], totalElements: 0, totalPages: 0, page: 0,
    };

    it('doet een POST naar /data met de juiste payload', () => {
      const req = { filters: [], sort: null, page: 0, size: 50 };
      service.query(req).subscribe();

      const call = http.expectOne(`${environment.apiBaseUrl}/data`);
      expect(call.request.method).toBe('POST');
      expect(call.request.body).toEqual(req);
      call.flush(mockResult);
    });

    it('retourneert de serverrespons als Observable', (done) => {
      const result: PageResult = {
        content: [{ roofvogelName: 'Havik' } as any],
        totalElements: 1,
        totalPages: 1,
        page: 0,
      };
      service.query({ filters: [], sort: null, page: 0, size: 50 }).subscribe(r => {
        expect(r.totalElements).toBe(1);
        expect(r.content[0].roofvogelName).toBe('Havik');
        done();
      });
      http.expectOne(`${environment.apiBaseUrl}/data`).flush(result);
    });
  });

  describe('upload()', () => {
    it('doet een POST naar /upload met FormData', () => {
      const file = new File(['<xml/>'], 'test.xml', { type: 'application/xml' });
      service.upload(file, 'file').subscribe();

      const call = http.expectOne(`${environment.apiBaseUrl}/upload`);
      expect(call.request.method).toBe('POST');
      expect(call.request.body).toBeInstanceOf(FormData);
      call.flush({ status: 'ok', roofvogels: 2 });
    });

    it('rapporteert progress: 100 en resultaat bij succesvolle respons', (done) => {
      const file = new File(['<xml/>'], 'test.xml');
      const results: { progress: number; result?: Record<string, number> }[] = [];

      service.upload(file, 'file').subscribe({
        next: v => results.push(v),
        complete: () => {
          const final = results[results.length - 1];
          expect(final.progress).toBe(100);
          expect(final.result?.['roofvogels']).toBe(5);
          done();
        }
      });

      http.expectOne(`${environment.apiBaseUrl}/upload`).flush(
        { roofvogels: 5, dieren: 4, kippen: 3, slangen: 2 }
      );
    });
  });
});
