import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { COLUMNS, GROUP_SPANS, Group } from '../../models/column.model';
import { selectColumnVisibility, selectDensity } from '../../store/settings/settings.selectors';
import { selectMetadata } from '../../store/upload/upload.selectors';
import { FileMetadata } from '../../store/upload/upload.actions';
import * as SettingsActions from '../../store/settings/settings.actions';
import { Density } from '../../store/settings/settings.actions';

interface GroupWithColumns {
  label: string;
  group: Group;
  columns: { field: string; label: string }[];
}

@Component({
  selector: 'app-instellingen',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './instellingen.component.html',
  styleUrl: './instellingen.component.scss'
})
export class InstellingenComponent {
  readonly groups: GroupWithColumns[] = GROUP_SPANS.map(g => ({
    label: g.label,
    group: g.group as Group,
    columns: COLUMNS.filter(c => c.group === g.group).map(c => ({ field: c.field, label: c.label })),
  }));

  readonly previewRows = [
    { roofvogelName: 'Buizerd-1',   modelTypeDescription: 'Primary',   dierName: 'panda',  kipIpAddress: '192.168.1.10',  kipMacAddress: '00:1A:2B:3C:4D:5E', slangMask: '255.255.255.0',   dierVirtual: true  },
    { roofvogelName: 'Sperwer-2',   modelTypeDescription: 'Secondary', dierName: 'tiger',  kipIpAddress: '10.0.0.45',     kipMacAddress: 'A1:B2:C3:D4:E5:F6', slangMask: '255.255.0.0',     dierVirtual: false },
    { roofvogelName: 'Havik-3',     modelTypeDescription: 'Core',      dierName: 'lion',   kipIpAddress: '172.16.5.22',   kipMacAddress: 'F0:E1:D2:C3:B4:A5', slangMask: '255.255.255.128', dierVirtual: true  },
    { roofvogelName: 'Torenvalk-4', modelTypeDescription: 'Edge',      dierName: 'bear',   kipIpAddress: '192.168.10.1',  kipMacAddress: '11:22:33:44:55:66', slangMask: '255.255.255.0',   dierVirtual: false },
  ];

  openGroups  = new Set<string>();
  visibility$: Observable<Record<string, boolean>>;
  metadata$:   Observable<FileMetadata | null>;
  density$:    Observable<Density>;

  constructor(private store: Store) {
    this.visibility$ = store.select(selectColumnVisibility);
    this.metadata$   = store.select(selectMetadata);
    this.density$    = store.select(selectDensity);
  }

  setDensity(density: Density) {
    this.store.dispatch(SettingsActions.setDensity({ density }));
    localStorage.setItem('density', density);
  }

  toggleOpen(group: string) {
    this.openGroups.has(group) ? this.openGroups.delete(group) : this.openGroups.add(group);
  }

  isOpen(group: string): boolean { return this.openGroups.has(group); }

  toggle(field: string) { this.store.dispatch(SettingsActions.toggleColumn({ field })); }

  reset() { this.store.dispatch(SettingsActions.resetColumns()); this.openGroups.clear(); }

  activeCount(cols: { field: string }[], vis: Record<string, boolean>): number {
    return cols.filter(c => vis[c.field]).length;
  }

  displayName(filename: string): string {
    const base = filename.replace(/\.[^/.]+$/, '');
    return base.charAt(0).toUpperCase() + base.slice(1);
  }

  fileFormat(filename: string): string {
    return (filename.split('.').pop() ?? 'FILE').toUpperCase();
  }

  format(n: number): string {
    return n.toLocaleString('nl-NL');
  }

  barWidth(count: number, counts: FileMetadata['counts']): number {
    if (!counts || count === 0) return 0;
    const max = Math.max(counts.roofvogels, counts.dieren, counts.kippen, counts.slangen, 1);
    return Math.round(100 * Math.sqrt(count / max));
  }
}
