--
-- Client independant Master Data
-- 
\i 'Master Data/AssetType.sql'
\i 'Master Data/ExternalIdType.sql'
\i 'Master Data/TransactionTypes.sql'
\i 'Master Data/Units.sql'
\i 'Master Data/FieldNamesMerged.sql'


--
-- Client dependant Master Data (using its OWN SCHEMA)
--
DELETE FROM dtpw.ref_branch;
COPY dtpw.ref_branch (k,v)
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/Branch.csv' DELIMITER ',' CSV HEADER;

DELETE FROM dtpw.ref_chief_directorate;
COPY dtpw.ref_chief_directorate (k,v,branch_code)
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/ChiefDirectorate.csv' DELIMITER ',' CSV HEADER;

DELETE FROM dtpw.ref_client_department;
COPY dtpw.ref_client_department (k,v,chief_directorate_code)
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/ClientDepartment.csv' DELIMITER ',' CSV HEADER;


DELETE FROM asset.ref_facility_type;
COPY asset.ref_facility_type (k,v)
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/FacilityType.csv' DELIMITER ',' CSV HEADER;


UPDATE dtpw.ref_client_department
SET responsible_dept_classif = 
	text2ltree(
		(SELECT branch_code FROM dtpw.ref_chief_directorate WHERE dtpw.ref_chief_directorate.k = dtpw.ref_client_department.chief_directorate_code ) || '.' || 
		chief_directorate_code || '.' || 
		k);


-- This must follow the imports above! 
\i 'Master Data/AccessControl.sql'



--
-- Import the asset data
--
DROP TABLE IF EXISTS asset_import;

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
	"X" text,   -- Longitude (Optinoal)
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
    "Department" text,  -- (Optional)

	"LocationAddress" text,  -- (Optional)
	"Geometry" text, -- (Optional)
	"Serial_Number" text  UNIQUE, -- (Optional) 
	"Barcode" text UNIQUE, -- (Optional)
	"EMIS" text -- (Optional)
);

DELETE FROM asset_import;
COPY asset_import 
FROM 'C:/Users/frankvr/Documents/Core/DTPW Data/DTPW_Location Breakdown_V13B_20200212.csv' DELIMITER ',' CSV HEADER;



\i 'Conversion/00_asset.sql'  
\i 'Conversion/11_attributes.sql'
\i 'Conversion/12_Land Parcels.sql'
\i 'Conversion/13_Departments.sql'
\i 'Conversion/14_SetAccess.sql'

\i 'DTPW Data/emis.sql'

--DELETE FROM postal_code;
--COPY  postal_code (suburb, box_code, street_code, area)
--FROM 'C:/Users/frankvr/Documents/Core/Master Data/postalcodes.csv' DELIMITER ';' CSV HEADER;

\i 'import_report.sql'
