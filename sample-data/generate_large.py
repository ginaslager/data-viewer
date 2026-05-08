#!/usr/bin/env python3
"""Generates a large roofvogels.xml test file (~500 MB)."""

import random
import sys

ROOFVOGEL_NAMEN = [
    "Zeearend","Buizerd","Havik","Sperwer","Torenvalk","Slechtvalk",
    "Blauwe Kiekendief","Bruine Kiekendief","Grauwe Kiekendief",
    "Wespendief","Rode Wouw","Zwarte Wouw","Visarend","Koningsgier","Steppearend"
]
DIER_NAMEN = [
    "Hond","Kat","Muis","Rat","Konijn","Haas","Egel","Vos","Das","Bunzing",
    "Hermelijn","Wezel","Eekhoorn","Mol","Veldmuis","Waterrat","Hamster",
    "Marmot","Ree","Wild zwijn"
]
FUNCTIE_NAMEN = [
    "Jagen","Nestelen","Fourageren","Verkennen","Bewaken","Aanvallen",
    "Verdedigen","Patrouilleren","Territorium afbakenen","Prooi lokaliseren",
    "Communiceren","Broeden"
]
KIP_NAMEN = [
    "Bo","Fien","Kees","Ans","Dirk","Lies","Piet","Marie","Jan","Els",
    "Cor","Nel","Roos","Sjaak","Wim","Lien","Ad","Jo","Ben","Tom",
    "Griet","Henk","Ria","Bert","Noor","Lotte","Daan","Eva","Stef","Kim"
]
SLANG_NAMEN = [
    "Cobra","Python","Anaconda","Mamba","Viper","Boa","Adder","Ringslang",
    "Gladde slang","Rattenslang","Koningsslang","Tijgerslang","Koraalslang",
    "Boomslang","Waterslang"
]

# Tuning: 300 roofvogels, 60 dieren, 90 kippen → ~500 MB
NUM_ROOFVOGELS  = 660
DIEREN_PER_RV   = 60
FUNCTIES_PER_DIER = 4
KIPPEN_PER_DIER = 90
SLANGEN_PER_RV  = 40

GIT_SHA     = 'a3f9c2b1d4e7f099ab23cd45ef678012'
DESCRIPTION = 'Jaaroverzicht monitoring 2025'
CREATE_DATE = '2025-04-12'

def main():
    out = sys.stdout
    total_dieren  = NUM_ROOFVOGELS * DIEREN_PER_RV
    total_kippen  = NUM_ROOFVOGELS * DIEREN_PER_RV * KIPPEN_PER_DIER
    total_slangen = NUM_ROOFVOGELS * SLANGEN_PER_RV

    out.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    out.write('<roofvogels>\n')
    out.write('  <metadata>\n')
    out.write(f'    <createDate>{CREATE_DATE}</createDate>\n')
    out.write(f'    <gitSha>{GIT_SHA}</gitSha>\n')
    out.write(f'    <description>{DESCRIPTION}</description>\n')
    out.write('    <entityCounts>\n')
    out.write(f'      <totalRoofvogels>{NUM_ROOFVOGELS}</totalRoofvogels>\n')
    out.write(f'      <totalDieren>{total_dieren}</totalDieren>\n')
    out.write(f'      <totalKippen>{total_kippen}</totalKippen>\n')
    out.write(f'      <totalSlangen>{total_slangen}</totalSlangen>\n')
    out.write('    </entityCounts>\n')
    out.write('  </metadata>\n')

    rv_id   = 0
    dier_id = 0
    func_id = 0
    kip_id  = 0
    slang_id = 0

    for ri in range(NUM_ROOFVOGELS):
        rv_id += 1
        rv_naam = ROOFVOGEL_NAMEN[ri % len(ROOFVOGEL_NAMEN)]
        out.write(f'  <roofvogel>\n')
        out.write(f'    <id>{rv_id}</id>\n')
        out.write(f'    <naam>{rv_naam} {rv_id}</naam>\n')

        # slangen block first so kipSlangId can reference them
        slang_start = slang_id + 1
        slang_ids_this_rv = []
        out.write(f'    <slangen>\n')
        for si in range(SLANGEN_PER_RV):
            slang_id += 1
            slang_ids_this_rv.append(slang_id)
            s_naam = SLANG_NAMEN[si % len(SLANG_NAMEN)]
            out.write(f'      <slang>\n')
            out.write(f'        <id>{slang_id}</id>\n')
            out.write(f'        <naam>{s_naam} {slang_id}</naam>\n')
            out.write(f'      </slang>\n')
        out.write(f'    </slangen>\n')

        out.write(f'    <dieren>\n')
        for di in range(DIEREN_PER_RV):
            dier_id += 1
            d_naam = DIER_NAMEN[di % len(DIER_NAMEN)]
            out.write(f'      <dier>\n')
            out.write(f'        <id>{dier_id}</id>\n')
            out.write(f'        <naam>{d_naam} {dier_id}</naam>\n')

            out.write(f'        <functies>\n')
            for fi in range(FUNCTIES_PER_DIER):
                func_id += 1
                f_naam = FUNCTIE_NAMEN[(di * FUNCTIES_PER_DIER + fi) % len(FUNCTIE_NAMEN)]
                out.write(f'          <functie>\n')
                out.write(f'            <id>{func_id}</id>\n')
                out.write(f'            <naam>{f_naam}</naam>\n')
                out.write(f'          </functie>\n')
            out.write(f'        </functies>\n')

            out.write(f'        <kippen>\n')
            for ki in range(KIPPEN_PER_DIER):
                kip_id += 1
                k_naam = KIP_NAMEN[ki % len(KIP_NAMEN)]
                linked_slang = slang_ids_this_rv[ki % len(slang_ids_this_rv)]
                out.write(f'          <kip>\n')
                out.write(f'            <id>{kip_id}</id>\n')
                out.write(f'            <naam>{k_naam} {kip_id}</naam>\n')
                out.write(f'            <kipSlangId>{linked_slang}</kipSlangId>\n')
                out.write(f'          </kip>\n')
            out.write(f'        </kippen>\n')

            out.write(f'      </dier>\n')
        out.write(f'    </dieren>\n')
        out.write(f'  </roofvogel>\n')

        if (ri + 1) % 10 == 0:
            print(f'  {ri+1}/{NUM_ROOFVOGELS} roofvogels...', file=sys.stderr)

    out.write('</roofvogels>\n')
    print(f'\nKlaar — rv:{rv_id}  dier:{dier_id}  kip:{kip_id}  slang:{slang_id}', file=sys.stderr)

if __name__ == '__main__':
    main()
