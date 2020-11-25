Import specification
====================

 Data specification (General)
 -----------------------------

> **NOTE**: Import data must be **UTF-8**


## Codes and key values ('k') 
 * Are varchar(10) unless otherwise specified
 * Valid characters are 'a-zA-Z0-9_'
 * Case sensitive
 * Our **convention** is to use ALL CAPS

This means uou must *remove whitespace* from such strings.

## Path values
A path is a dot ('.') delimited sequence of path segments. A segment

*  Valid characters are 'a-zA-Z0-9_'
*  Case sensitive
  
A path has a maximum length of 100 characters.


DTPW data
---------

## Pre-defined master data

* The master data must be loaded into the system before asset imports can happen.  
* Subsequent loads of lookups will merge with existing data meaning that updates can be incremental.

See the [Template](import_template.csv)

> All fields are **mandatory**

|Type|Description|File|Format|
|----|-----------|----|-------|
|Branch|Branch data|csv|k,v|
|Chief Directorate|Chief Directorate and mapping to Branch. Use k from Branch.csv as `branch` |csv|k,v,branch|
|Client Department|Client Department and mapping to Chief Directorate. Use k from ChiefDirectorate.csv as `directorate_code` |csv|k,v,directorate_code|
|Facility Type|Facility Type |csv|k,v|
|District|District|csv|k,v|
|Region|Region|csv|k,v|
|Municipality|Municipality|csv|k,v|
|Suburb|Suburb|csv|k,v|
|Town|Town|csv|k,v|


## Importing asset data 

* This is the `csv` format that we will use to import and export data  
* The ordering of fields is not important
* It is **case sensitive**

> Note that if the asset_id column is populated it must contain the asset_id (UUID) of an existing asset and will result in an update of that asset. .If you do not specify an asset_id a new asset will be created and a UUID will be assigned to it.

|Field                  |Description|M/O|Applies to|
|-----------------------|-----------|---|----------|
|asset_id				|UUID. If populated the system will upsert this asset. Leave blank for insert|o|ALL|
|asset_type_code				|Text. One of `ENVELOPE`, `FACILITY`, `SITE`, `BUILDING`, `FLOOR`, `ROOM`, `LANDPARCEL`,`COMPONENT` (case sensitive) |m|ALL|
|name					|Free format Text|m|ALL|
|func_loc_path			|Path value|m|ALL|
|active					|Currently ignored|o|ALL|
|latitude				|Text|o|ALL|
|longitude				|Text|o|ALL|
|address				|Free format text|o|ALL|
|barcode				|Text|o|ALL|
|serial_number			|Master data (k)|o|ALL|
|district_code			|Master data (k)|o|`ENVELOPE`|
|municipality_code		|Master data (k)|o|`ENVELOPE`|
|town_code				|Master data (k)|o|`ENVELOPE`|
|suburb_code			|Master data (k)|o|`ENVELOPE`|
|facility_type_code		|Master data (k)|m|`FACILITY`|
|responsible_dept_code	|Master data (k)|o|ALL|
|is_owned				|TRUE=owned|o|ALL|
|EMIS					|EI EMIS number |o|ALL|
|LPI					|Land Parcel Identifier |o|`LANDPARCEL`|
|geom					|In wellknown text format|o|ALL|


## Associating Assets to Landparcels

This is imported separately from the asset data as there is 1:many relationship between the Assets living on LANDPARCELS and the parcels.

* This is the `csv` format that we will use to import and export data  
* The ordering of fields is not important
* It is **case sensitive**

> Both the Asset and LANDPARCELS must already exist.

|Field               |Description                                   |M/O|
|--------------------|----------------------------------------------|---|
|asset_id	         |Asset identifier of the asset e.g. Building   |m  |
|landparcel_asset_id |Asset identifier of LANDPARCEL                |m  |