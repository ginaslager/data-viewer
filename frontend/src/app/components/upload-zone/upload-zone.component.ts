import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable, take } from 'rxjs';
import * as UploadActions from '../../store/upload/upload.actions';
import { FileMetadata } from '../../store/upload/upload.actions';
import { selectMode, selectStatus, selectProgress, selectError } from '../../store/upload/upload.selectors';

@Component({
  selector: 'app-upload-zone',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upload-zone.component.html',
  styleUrl: './upload-zone.component.scss'
})
export class UploadZoneComponent {
  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  mode$:     Observable<'file' | 'database' | null>;
  status$:   Observable<string>;
  progress$: Observable<number>;
  error$:    Observable<string | null>;

  dragging = false;

  constructor(private store: Store) {
    this.mode$     = this.store.select(selectMode);
    this.status$   = this.store.select(selectStatus);
    this.progress$ = this.store.select(selectProgress);
    this.error$    = this.store.select(selectError);
  }

  selectMode(mode: 'file' | 'database') {
    this.store.dispatch(UploadActions.setMode({ mode }));
  }

  openFilePicker() {
    this.fileInputRef.nativeElement.value = '';
    this.fileInputRef.nativeElement.click();
  }

  onDragOver(e: DragEvent) { e.preventDefault(); this.dragging = true; }
  onDragLeave()            { this.dragging = false; }

  onDrop(e: DragEvent) {
    e.preventDefault();
    this.dragging = false;
    const file = e.dataTransfer?.files[0];
    if (file) this.dispatchUpload(file);
  }

  onFileSelect(e: Event) {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (file) this.dispatchUpload(file);
  }

  private dispatchUpload(file: File) {
    this.store.select(selectMode).pipe(take(1)).subscribe(mode => {
      this.store.dispatch(UploadActions.uploadFile({ file, mode: mode ?? 'file', fileName: file.name }));
    });
    this.extractMetadata(file).then(metadata => {
      if (metadata) this.store.dispatch(UploadActions.setMetadata({ metadata }));
    });
  }

  private async extractMetadata(file: File): Promise<FileMetadata | null> {
    try {
      const ext  = file.name.split('.').pop()?.toLowerCase() ?? '';
      const text = await file.slice(0, 4096).text();

      if (ext === 'json') {
        return {
          filename:    file.name,
          createDate:  this.jsonField(text, 'createDate'),
          gitSha:      this.jsonField(text, 'gitSha'),
          description: this.jsonField(text, 'description'),
          counts:      null,
        };
      }

      const xmlGet = (tag: string): string | null => {
        const m = text.match(new RegExp(`<${tag}>([^<]+)<\\/${tag}>`));
        return m ? m[1].trim() : null;
      };
      return {
        filename:    file.name,
        createDate:  xmlGet('createDate'),
        gitSha:      xmlGet('gitSha'),
        description: xmlGet('description'),
        counts:      null,
      };
    } catch {
      return { filename: file.name, createDate: null, gitSha: null, description: null, counts: null };
    }
  }

  private jsonField(text: string, field: string): string | null {
    const m = text.match(new RegExp(`"${field}"\\s*:\\s*"([^"]+)"`));
    return m ? m[1] : null;
  }
}
