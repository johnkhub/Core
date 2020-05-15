Tricks
=========


Identify broken paths
---------------------

Firstly find the broken records
```
SELECT *
INTO broken_paths
FROM asset
WHERE fn_is_valid_func_loc_path(func_loc_path) = false
```
then find the top most broken nodes (by definition `ENVELOPE` must exist)

```
SELECT distinct subpath(func_loc_path,0,2),asset_type_code 
FROM broken_paths 
WHERE asset_type_code IN ('SITE', 'BUILDING', 'FACILITY')
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