---
--- Tries to tie up v6 facility data so we can get it into the v8 facility table
---

-- Set code to row id and zero pad
update AssetRegisterFacility set AssetFacilityCode =  REPLICATE('0',10-LEN(RTRIM(CONVERT(varchar(10),AssetFacilityID)))) + CONVERT(nvarchar(20),AssetFacilityID)

-- Generate insert script for postgres
select 'INSERT INTO facility (code,name,facility_type_code) VALUES (''' + AssetFacilityCode + ''',''' + REPLACE(AssetFacilityName, '''', '''''') + ''',''GENERIC'');' from AssetRegisterFacility 