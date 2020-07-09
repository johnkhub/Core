Tricks
=========

Validating imports/db consistency
-------------------------------------

## Stats

```
SELECT * FROM import_report_view
```

```
SELECT 
	(
	  SELECT count(asset_id) FROM asset.a_tp_envelope WHERE municipality_code IS NULL
	) AS "Missing municipality codes",
	(
	  SELECT count(asset_id) FROM asset.a_tp_envelope WHERE town_code IS NULL
	) AS "Missing town codes",
	(
	  SELECT count(asset_id) FROM asset.a_tp_envelope WHERE suburb_code IS NULL 
	) AS "Missing suburb codes",
	(
	  SELECT count(asset_id) FROM asset.a_tp_envelope WHERE suburb_code IS NULL 
	) AS "Missing district codes"
;
```

```
SELECT count(a.asset_id) "Facilities with no facility code"
FROM 
  asset AS a LEFT JOIN asset.a_tp_facility f  ON a.asset_id = f.asset_id
WHERE a.asset_type_code = 'FACILITY' AND (f.asset_id IS NULL OR f.facility_type_code IS NULL);

SELECT
  (SELECT count(distinct ("LocationSGcode FROM FAR")) from land_parcel_import) AS "Num distinct features",
  (SELECT count(*) FROM asset where asset_type_code = 'LANDPARCEL') AS "Num added parcels",
  (SELECT count(*) FROM asset.a_tp_landparcel) AS "Num added parcels ext",
  (SELECT count(*) FROM asset.landparcel_view) AS "Num correctly joined extensions",
  (
    SELECT count(a.asset_id) FROM asset.a_tp_landparcel a LEFT JOIN asset.asset_landparcel alp ON a.asset_id = alp.landparcel_asset_id
    WHERE alp.landparcel_asset_id IS NULL
  ) AS "Lanparcels not linked to envelope";
```

## Identify broken paths

Firstly find the broken records
```
SELECT *
INTO broken_paths
FROM asset
WHERE fn_is_valid_func_loc_path(func_loc_path) = false
```
then find the top most broken nodes (by definition `ENVELOPE` must exist)

```
select * from asset where func_loc_path 
in (
select distinct subpath(func_loc_path,0,2)
FROM broken_paths 
WHERE asset_type_code IN ('SITE', 'BUILDING')
)
```


```
select count(*) from asset_import where asset_id is null

select count(*) from asset_import where "Level" = 'Landparcel'

select count(*) from 
asset 
right join asset_import 
on asset.code = asset_import."Code (uk)"
where asset.asset_id  is not null

select count(*) from asset where asset_type_code not in ('ENVELOPE','FACILITY')

select count(*) from dtpw.dtpw_core_report_view 

select * fromasset_import where

select 
(select count(*) from asset_import where "Level" = 'Asset') as "Assets",
(select count(*) from asset_import where "Level" = 'Facility' ) as "Facilities",
(select count(*) from asset_import where "Level" = 'Building') as "Building",
(select count(*) from asset_import where "Level" = 'Site') as "Site",
(select count(*) from asset_import where "Level" = 'Floor') as "Floor",
(select count(*) from asset_import where "Level" = 'Room') as "Room"

select 
(select count(*) from asset where asset_type_code = 'ENVELOPE') as "Assets",
(select count(*) from asset where asset_type_code = 'FACILITY' ) as "Facilities",
(select count(*) from asset where asset_type_code =  'BUILDING') as "Building",
(select count(*) from asset where asset_type_code =  'SITE') as "Site",
(select count(*) from asset where asset_type_code =  'FLOOR') as "Floor",
(select count(*) from asset where asset_type_code =  'ROOM') as "Room"


```

Backdoor API
--------------

Delete record(s)

> This must be used with **EXTREME** caution. There is currently no logging of this
> operation. Needless to say there is also no undo.

```
DELETE FROM fn_delete_asset(asset_id) WHERE code like'20358A%'
```


Admin
---------------------------

``
SELECT  pg_size_pretty (
	(
		pg_indexes_size('public.asset') +
		pg_indexes_size('public.asset_classification') +
		pg_indexes_size('public.asset_identification') +
		pg_indexes_size('public.asset_link') +
		pg_indexes_size('public.asset_tags') +
		pg_indexes_size('public.location') +
		pg_indexes_size('public.geoms') +
		
		pg_indexes_size('asset.a_tp_building') +
		pg_indexes_size('asset.a_tp_component') +
		pg_indexes_size('asset.a_tp_envelope') +
		pg_indexes_size('asset.a_tp_facility') +
		pg_indexes_size('asset.a_tp_floor') +
		pg_indexes_size('asset.a_tp_room') +
		pg_indexes_size('asset.a_tp_site') +
		pg_indexes_size('asset.a_tp_landparcel') +
		pg_indexes_size('asset.asset_landparcel') 
		
	) / (select count(*) from asset)
) as "Index bytes / asset";

SELECT  pg_size_pretty (
	(
		pg_relation_size('public.asset') +
		pg_relation_size('public.asset_classification') +
		pg_relation_size('public.asset_identification') +
		pg_relation_size('public.asset_link') +
		pg_relation_size('public.asset_tags') +
		pg_relation_size('public.location') +
		pg_relation_size('public.geoms') +
		
		pg_relation_size('asset.a_tp_building') +
		pg_relation_size('asset.a_tp_component') +
		pg_relation_size('asset.a_tp_envelope') +
		pg_relation_size('asset.a_tp_facility') +
		pg_relation_size('asset.a_tp_floor') +
		pg_relation_size('asset.a_tp_room') +
		pg_relation_size('asset.a_tp_site') +
		pg_relation_size('asset.a_tp_landparcel') +
		pg_relation_size('asset.asset_landparcel') 
	) / (select count(*) from asset)
) as "Table bytes / asset"


``

```
WITH cteTableInfo AS 
(
	SELECT 
		COUNT(1) AS ct
		,SUM(length(t::text)) AS TextLength  
		,'public.asset'::regclass AS TableName  
	FROM public.asset AS t  
)
,cteRowSize AS 
(
   SELECT ARRAY [pg_relation_size(TableName)
               , pg_relation_size(TableName, 'vm')
               , pg_relation_size(TableName, 'fsm')
               , pg_table_size(TableName)
               , pg_indexes_size(TableName)
               , pg_total_relation_size(TableName)
               , TextLength
             ] AS val
        , ARRAY ['Total Relation Size'
               , 'Visibility Map'
               , 'Free Space Map'
               , 'Table Included Toast Size'
               , 'Indexes Size'
               , 'Total Toast and Indexes Size'
               , 'Live Row Byte Size'
             ] AS Name
   FROM cteTableInfo
)
SELECT 
	unnest(name) AS Description
	,unnest(val) AS Bytes
	,pg_size_pretty(unnest(val)) AS BytesPretty
	,unnest(val) / ct AS bytes_per_row
FROM cteTableInfo, cteRowSize
 
UNION ALL SELECT '------------------------------', NULL, NULL, NULL
UNION ALL SELECT 'TotalRows', ct, NULL, NULL FROM cteTableInfo
UNION ALL SELECT 'LiveTuples', pg_stat_get_live_tuples(TableName), NULL, NULL FROM cteTableInfo
UNION ALL SELECT 'DeadTuples', pg_stat_get_dead_tuples(TableName), NULL, NULL FROM cteTableInfo;
```

Bad lat/lon

```
select * from location where latitude < -35.0 or latitude > -30.0;
select * from location where longitude < 17.0 or longitude > 24.0;
```