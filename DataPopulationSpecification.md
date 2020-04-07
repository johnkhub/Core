Import specification
====================

 Data specification
 --------------------
**NOTE**: Import data must be UTF-8


## Codes and key values ('k') 
 * Are varchar(10) unless otherwise specified
 * Valid characters are 'a-zA-Z0-9_'
 * Case sensitive
 * Our **convention** is to use ALL CAPS

This means *removing whitespace* from such strings.

## Path values
A path is a dash ('-') delimited sequence of path segments. A segment

*  Valid characters are 'a-zA-Z0-9_'
*  Case sensitive
  
A path has a maximum length of 100 characters.


DTPW data
---------

## Pre-defined master data
Predefined data must be prepared upfront.

|Type|Description|File|Format|
|----|-----------|----|-------|
|Branch|Branch data|Branch.csv|k,v|
|Chief Directorate|Chief Directorate and mapping to Branch. Use k from Branch.csv as `branch` |Branch.csv|k,v,branch|
|Client Department|Client Department and mapping to Chief Directorate. Use k from ChiefDirectorate.csv as `directorate_code` |Branch.csv|k,v,directorate_code|
|Facility Type|Facility Type |FacilityType.csv|k,v|


## Imported asset data

The Code field is actually a Path and as such must adhere to the requirements of a Path as listed above.  Code must **uniquely** identify an asset.  

This data will result in the automatic population of some lookup tables.

* District
* Municipality
* Suburb
* Town

*This means that the codes are fabricated by the system.*

Note that the data must contain valid values (as defined in the predefined master data for) Facility Type as "AssetTypeID" and Department.

The data must fit this table definition
```
CREATE TABLE asset_import 
(
	"AssetID" text NOT NULL CHECK("AssetID" ~ '^([a-zA-Z0-9_]+([-][a-zA-Z0-9_]+)*)$'), -- Mandatory
	"Facility/Asset Name" text, -- Mandatory
	"Level" text NOT NULL CHECK("Level" = 'Asset' OR  "Level" = 'Facility' OR "Level" = 'Building' OR "Level" = 'Site' OR "Level" = 'Floor' OR "Level" = 'Room'), -- Mandatory
	"Level (info)" text, -- Mandatory
	"Code (uk)" text  NOT NULL CHECK ("Code (uk)" ~ '^([a-zA-Z0-9_]+([-][a-zA-Z0-9_]+)*)$'), -- Mandatory
	

	"Code Name" text, -- IGNORED
	"SubCategory" text, -- IGNORED
	"Type" text, -- IGNORED
	"Code(uk)_V9" text, -- IGNORED

    "Y" text,   -- Latitude (Optional)
	"X" text,   -- Longitude (Optional)
	"Suburb_Nam" text, -- (Optional)
	"Suburb_Id" text,  -- (Optional)
	
	"PRCL_KEY" text, -- IGNORED
	"TAG_X" text, -- IGNORED
	"TAG_Y" text, -- IGNORED
	"TAG_VALUE" text, -- IGNORED
	"ID" text, -- IGNORED
	"PARCEL_NO" text, -- IGNORED
	"PORTION" text, -- IGNORED
	"COUNTRY" text, -- IGNORED
	"PROVINCE" text, -- IGNORED
	
	"DISTRICT" text, -- (Optional)
	"LOCAL" text, -- (Optional)
	"TOWN" text, -- (Optional)
	"MAIN_NAME" text, -- IGNORED
	"SUBURB" text, --  (Optional)
	"Area" text, -- IGNORED


    "AssetTypeID" text NULL CHECK ("AssetTypeID" ~ '^([a-zA-Z0-9_]*)$'), -- (Optional)
	"AssetTypeName" text NULL,  -- IGNORED
	
	"SubCategoryID2" text NULL, -- IGNORED
	"SubCategory2" text NULL, -- IGNORED
	
    "MapFeatureID_UPDATED_BY_IMQS" text UNIQUE CHECK ("MapFeatureID_UPDATED_BY_IMQS" ~ '^([a-zA-Z0-9_]*)$'), -- LPI  (Optional)
    "Department" text,
	  -- (Optional)	
	"LocationAddress" text,  -- (Optional)
	"Geometry" text -- (Optional) In wellknown text format
);
```
* `IGNORED` fields are ignore and we may consider removing them from the source csv file and the table
* `Optional` fields are optional and are supplied if we have the data
* `Mandatory` fields must be present in each row in the csv file


## Land Parcels 

This is imported separately from the asset data as there is 1:many relationship between assets and land parcels.

```
CREATE TABLE IF NOT EXISTS land_parcel_import (
    "AssetId" text NOT NULL,
    "ClientAssetID" text, -- IGNORED
    "AssetFacilityName" text, -- IGNORED
    "LocationSGcode FROM FAR" text, -- IGNORED
    "MapFeatureID UPDATED BY IMQS" text NOT NULL
);
```

