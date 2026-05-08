export type Group = 'roofvogel' | 'dier' | 'functies' | 'kip' | 'slang';

export interface Column {
  field:    string;
  label:    string;
  group:    Group;
  sortable: boolean;
  visible:  boolean;
  type?:    'boolean';
}

export interface GroupSpan {
  label: string;
  group: string;
  span:  number;
}

export const COLUMNS: Column[] = [
  // ── Roofvogel ────────────────────────────────────────────────────────────────
  { field: 'roofvogelName',                 label: 'Name',        group: 'roofvogel', sortable: true,  visible: true  },
  { field: 'roofvogelType',                 label: 'Type',        group: 'roofvogel', sortable: true,  visible: false },
  { field: 'roofvogelModelType',            label: 'Model Type',  group: 'roofvogel', sortable: true,  visible: false },
  { field: 'roofvogelModelTypeDescription', label: 'Description', group: 'roofvogel', sortable: true,  visible: true  },
  { field: 'roofvogelNumber',               label: 'Number',      group: 'roofvogel', sortable: true,  visible: false },
  // ── Dier ─────────────────────────────────────────────────────────────────────
  { field: 'dierName',                      label: 'Name',        group: 'dier',      sortable: true,  visible: true  },
  { field: 'dierRole',                      label: 'Role',        group: 'dier',      sortable: true,  visible: false },
  { field: 'dierType',                      label: 'Type',        group: 'dier',      sortable: true,  visible: false },
  { field: 'dierTypeDescription',           label: 'Type Descr.', group: 'dier',      sortable: true,  visible: false },
  { field: 'dierTypeNumber',                label: 'Type Number', group: 'dier',      sortable: false, visible: false },
  { field: 'dierVirtual',                   label: 'Virtual',     group: 'dier',      sortable: true,  visible: true,  type: 'boolean' },
  // ── Functies ──────────────────────────────────────────────────────────────────
  { field: 'functions',                     label: 'Functions',   group: 'functies',  sortable: false, visible: false },
  { field: 'services',                      label: 'Services',    group: 'functies',  sortable: false, visible: false },
  // ── Kip ──────────────────────────────────────────────────────────────────────
  { field: 'kipIpAddress',                  label: 'IP Address',  group: 'kip',       sortable: true,  visible: true  },
  { field: 'kipMacAddress',                 label: 'MAC Address', group: 'kip',       sortable: true,  visible: true  },
  { field: 'kipType',                       label: 'Type',        group: 'kip',       sortable: true,  visible: false },
  { field: 'kipSlangId',                    label: 'Slang ID',    group: 'kip',       sortable: true,  visible: false },
  // ── Slang ─────────────────────────────────────────────────────────────────────
  { field: 'slangId',                       label: 'ID',          group: 'slang',     sortable: true,  visible: false },
  { field: 'slangDescription',              label: 'Description', group: 'slang',     sortable: true,  visible: false },
  { field: 'slangMask',                     label: 'Mask',        group: 'slang',     sortable: true,  visible: true  },
  { field: 'slangNetworkAddress',           label: 'Network Addr',group: 'slang',     sortable: true,  visible: false },
  { field: 'slangType',                     label: 'Type',        group: 'slang',     sortable: true,  visible: false },
];

export const GROUP_SPANS: GroupSpan[] = [
  { label: 'ROOFVOGEL', group: 'roofvogel', span: 5 },
  { label: 'DIER',      group: 'dier',      span: 6 },
  { label: 'FUNCTIES',  group: 'functies',  span: 2 },
  { label: 'KIP',       group: 'kip',       span: 4 },
  { label: 'SLANG',     group: 'slang',     span: 5 },
];

export const OPERATORS = [
  { value: 'contains',    label: 'contains'    },
  { value: 'startsWith',  label: 'starts with' },
  { value: 'endsWith',    label: 'ends with'   },
  { value: 'equals',      label: 'equals'      },
  { value: 'notEquals',   label: 'not equals'  },
];
