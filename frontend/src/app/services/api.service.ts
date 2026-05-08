import { Injectable } from '@angular/core';
import { HttpClient, HttpEventType, HttpResponse } from '@angular/common/http';
import { Observable, filter, map } from 'rxjs';
import { FlatRow } from '../models/flat-row.model';

const BASE = 'http://localhost:8082/api';

export interface DataRequest {
  filters: { field: string; operator: string; value: string }[];
  sort:    { field: string; direction: 'ASC' | 'DESC' } | null;
  page:    number;
  size:    number;
}

export interface PageResult {
  content:       FlatRow[];
  totalElements: number;
  totalPages:    number;
  page:          number;
}


@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  query(req: DataRequest): Observable<PageResult> {
    return this.http.post<PageResult>(`${BASE}/data`, req);
  }


  upload(file: File, mode: 'file' | 'database'): Observable<{ progress: number; result?: Record<string, number> }> {
    const fd = new FormData();
    fd.append('file', file);
    fd.append('mode', mode);
    return this.http.post<Record<string, number>>(`${BASE}/upload`, fd, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      filter(e => e.type === HttpEventType.UploadProgress || e.type === HttpEventType.Response),
      map(e => {
        if (e.type === HttpEventType.UploadProgress) {
          return { progress: Math.round(100 * (e.loaded / (e.total ?? e.loaded))) };
        }
        return { progress: 100, result: (e as HttpResponse<Record<string, number>>).body ?? {} };
      })
    );
  }
}
