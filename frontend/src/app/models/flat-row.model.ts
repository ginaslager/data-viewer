export interface FlatRow {
  // Roofvogel
  roofvogelName:                 string;
  roofvogelType:                 string | null;
  roofvogelModelType:            string | null;
  roofvogelModelTypeDescription: string | null;
  roofvogelNumber:               string | null;
  // Dier
  dierName:                      string;
  dierRole:                      string | null;
  dierType:                      string | null;
  dierTypeDescription:           string | null;
  dierTypeNumber:                string | null;
  dierVirtual:                   boolean | null;
  // Functies
  functions:                     string | null;
  services:                      string | null;
  // Kip
  kipIpAddress:                  string | null;
  kipMacAddress:                 string | null;
  kipType:                       string | null;
  kipSlangId:                    string | null;
  // Slang (matched via kipSlangId → slang.id)
  slangId:                       string | null;
  slangDescription:              string | null;
  slangMask:                     string | null;
  slangNetworkAddress:           string | null;
  slangType:                     string | null;
}
