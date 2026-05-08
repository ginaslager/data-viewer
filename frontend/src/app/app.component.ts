import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { UrlSyncService } from './services/url-sync.service';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
import { UploadZoneComponent } from './components/upload-zone/upload-zone.component';
import { DataTableComponent } from './components/data-table/data-table.component';
import { InstellingenComponent } from './components/instellingen/instellingen.component';
import { clearAllFilters } from './store/table/table.actions';
import { selectActiveFilterCount } from './store/table/table.selectors';
import { selectStatus } from './store/upload/upload.selectors';
import { selectTheme } from './store/settings/settings.selectors';
import * as SettingsActions from './store/settings/settings.actions';
import { Theme } from './store/settings/settings.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, UploadZoneComponent, DataTableComponent, InstellingenComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  activeFilterCount$: Observable<number>;
  theme$: Observable<Theme>;
  activeNav = 'overzicht';
  themeOpen = false;
  sidebarCollapsed = false;

  readonly themes: { id: Theme; label: string; bg: string }[] = [
    { id: 'carbon', label: 'Donker', bg: '#0E0E0E' },
    { id: 'slate',  label: 'Grijs',  bg: '#28282A' },
    { id: 'ivory',  label: 'Licht',  bg: '#EDEEF0' },
  ];

  private readonly destroyRef  = inject(DestroyRef);
  private readonly urlSync     = inject(UrlSyncService);

  constructor(private store: Store) {
    this.activeFilterCount$ = this.store.select(selectActiveFilterCount);
    this.theme$ = this.store.select(selectTheme);
  }

  ngOnInit() {
    this.urlSync.init();
    this.sidebarCollapsed = localStorage.getItem('sidebar-collapsed') === 'true';

    this.store.select(selectStatus)
      .pipe(filter(status => status === 'success'), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => { this.activeNav = 'overzicht'; });

    this.store.select(selectTheme)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(theme => document.documentElement.setAttribute('data-theme', theme));
  }

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
    localStorage.setItem('sidebar-collapsed', String(this.sidebarCollapsed));
  }

  clearFilters() { this.store.dispatch(clearAllFilters()); }

  chooseTheme(theme: Theme) {
    this.store.dispatch(SettingsActions.setTheme({ theme }));
    this.themeOpen = false;
  }

  themeLabel(id: Theme): string {
    return this.themes.find(t => t.id === id)?.label ?? id;
  }

  themeBg(id: Theme): string {
    return this.themes.find(t => t.id === id)?.bg ?? '#0E0E0E';
  }

  setNav(tab: string) {
    this.activeNav = tab;
  }
}
