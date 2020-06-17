-- IMPORT LOOKUPS HERE

-- districts
UPDATE asset.a_tp_envelope SET district_code = 'OVERBERG' WHERE district_code = 'OVERBERGDI';
UPDATE asset.a_tp_envelope SET district_code = 'CAPETOWN' WHERE district_code IN ('METROSOUTH', 'METROCENTR','CITYOFCAPE','CityofCape','METRONORTH','METROEAST');
UPDATE asset.a_tp_envelope SET district_code = 'EDEN' WHERE district_code IN ('EDENDISTRI', 'EDENANDCEN');
UPDATE asset.a_tp_envelope SET district_code = 'WESTCOAST' WHERE district_code = 'WESTCOASTD';

DELETE FROM asset.ref_district WHERE k IN ('OVERBERGDI','METROSOUTH', 'METROCENTR','CITYOFCAPE','CityofCape','METRONORTH','METROEAST','EDENDISTRI', 'EDENANDCEN','WESTCOASTD');

-- munic
UPDATE asset.a_tp_envelope SET municipality_code = 'CEDERBERG' WHERE municipality_code = 'CEDERBERGL';
UPDATE asset.a_tp_envelope SET municipality_code = 'MOSSELBAY' WHERE municipality_code = 'MOSSELBAYL';
UPDATE asset.a_tp_envelope SET municipality_code = 'LANGEBERG' WHERE municipality_code = 'LANGEBERGL';
UPDATE asset.a_tp_envelope SET municipality_code = 'KANNALAND' WHERE municipality_code = 'KANNALANDL';
UPDATE asset.a_tp_envelope SET municipality_code = 'KNYSNA' WHERE municipality_code = 'KNYSNALOCA';
UPDATE asset.a_tp_envelope SET municipality_code = 'BITOU' WHERE municipality_code = 'BITOULOCAL';
UPDATE asset.a_tp_envelope SET municipality_code = 'GEORGE' WHERE municipality_code = 'GEORGELOCA';
UPDATE asset.a_tp_envelope SET municipality_code = 'CAPETOWN' WHERE municipality_code = 'CITYOFCAPE';
UPDATE asset.a_tp_envelope SET municipality_code = 'SWARTLAND' WHERE municipality_code = 'SWARTLANDL';
UPDATE asset.a_tp_envelope SET municipality_code = 'HESSEQUA' WHERE municipality_code = 'HESSEQUALO';
UPDATE asset.a_tp_envelope SET municipality_code = 'MOSSELBAY' WHERE municipality_code = 'OUTENIQUAD';
UPDATE asset.a_tp_envelope SET municipality_code = 'CAPETOWN' WHERE municipality_code = 'CityofCape';

DELETE FROM asset.ref_municipality WHERE k
                                             IN ('CEDERBERGL','MOSSELBAYL','LANGEBERGL','KANNALANDL','KNYSNALOCA','BITOULOCAL','GEORGELOCA','CITYOFCAPE','SWARTLANDL','HESSEQUALO','OUTENIQUAD','CityofCape');

-- town
UPDATE asset.a_tp_envelope SET town_code = 'HEIDELBERG' WHERE town_code = 'HEIDELBERC';
UPDATE asset.a_tp_envelope SET town_code = 'KUILSRIVIR' WHERE town_code = 'KUILSRIVER';
UPDATE asset.a_tp_envelope SET town_code = 'LAMBERTSBY' WHERE town_code = 'LAMBERTSB';
UPDATE asset.a_tp_envelope SET town_code = 'MOORREESBG' WHERE town_code = 'MORREESBUG';
UPDATE asset.a_tp_envelope SET town_code = 'BEAUFORTWT' WHERE town_code = 'NELSPOORTT';
UPDATE asset.a_tp_envelope SET town_code = 'PIKETBERGG' WHERE town_code = 'BERGRIVIEL';
UPDATE asset.a_tp_envelope SET town_code = 'ELIMM' WHERE town_code = 'CAPEAGULHL';
UPDATE asset.a_tp_envelope SET town_code = 'CAPETOWNN' WHERE town_code = 'CITYOFCAPN';
UPDATE asset.a_tp_envelope SET town_code = 'DYSSELSDOP' WHERE town_code = 'DYSSELDORP';
UPDATE asset.a_tp_envelope SET town_code = 'EBENHAESAR' WHERE town_code = 'EBENHAEZER';
UPDATE asset.a_tp_envelope SET town_code = 'ELANDSBAAI' WHERE town_code = 'ELANDSBAYY';
UPDATE asset.a_tp_envelope SET town_code = 'KOEKENAAPP' WHERE town_code = 'MATZIKAMAL';
UPDATE asset.a_tp_envelope SET town_code = 'THEEWATERS' WHERE town_code = 'THEEWATERL';
UPDATE asset.a_tp_envelope SET town_code = 'CALEDONN' WHERE town_code = 'CALEDONMN';
UPDATE asset.a_tp_envelope SET town_code = NULL WHERE town_code = 'OUTSIDEATN';

DELETE FROM asset.ref_town WHERE k in (
                                       'HEIDELBERC','KUILSRIVER','LAMBERTSB','MORREESBUG','NELSPOORTT','BERGRIVIEL','CAPEAGULHL','CITYOFCAPN','DYSSELDORP',
                                       'EBENHAEZER','ELANDSBAYY','MATZIKAMAL','THEEWATERL','CALEDONMN','OUTSIDEATN'
    );