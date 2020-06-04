DROP TABLE "ORMJS_CACHE_LOCK_V1";
DROP TABLE "ORMJS_CACHE_INDEX_V1";
DROP TABLE asset_import;

DROP TABLE far.financials;
DROP TABLE far.lifecycle;
DROP SCHEMA far;

DROP TABLE transactions.transaction_batch CASCADE;
DROP TABLE transactions.transaction;
DROP TABLE transactions.field;
DROP TABLE transactions.transaction_type;
DROP SCHEMA transactions CASCADE;

DROP TABLE access_control.user;
DROP TABLE access_control.group;

COMMENT ON TABLE access_control.access_type IS 'Master data list of access types';
COMMENT ON COLUMN access_control.access_type.mask IS 'NONE=0  CREATE=1 READ=2,  UPDATE=4,  DELETE=8, ...';
COMMENT ON TABLE access_control.entity_access IS 'Defines what access specific principals have to specific entities';
COMMENT ON COLUMN access_control.entity_access.access_types IS 'Principal has these';
COMMENT ON COLUMN access_control.entity_access.grant_types IS 'Principal may grant these to others';
COMMENT ON TABLE access_control.principal IS 'Self referential table of principals. Groups are also principals. The table links users to groups via this self-reference';

ALTER TABLE public.asset DROP CONSTRAINT check_paths;


COMMENT ON FUNCTION public.fn_is_valid_func_loc_path(path public.ltree) IS 'Identifies if a segment of a path does not have a corresponding asset associated with it. E.g. select * from h(''11567.11567.BB.L1.09''::ltree) or select * into broken_paths from asset where fn_is_valid_func_loc_path(func_loc_path) = false';

CREATE INDEX asset_name_idx ON public.asset USING btree (name);

CREATE OR REPLACE FUNCTION public.fn_check_valid_func_loc_path(path ltree)
    RETURNS boolean
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    asset uuid;
BEGIN
    -- If n=1 this is the root of the path and obviously won't exist
    IF nlevel(path) = 1 THEN
        RETURN true;
    END IF;

    -- Only check up to n-1 as n is the node we are trying to insert
    FOR i IN 1..nlevel(path)-1
        LOOP
            asset := (SELECT asset_id from asset WHERE code = REPLACE(subpath(path,0,i)::text, '.', '-'));
            IF (asset IS null) THEN RETURN false;
            END IF;
        END LOOP;
    RETURN true;
END; $function$
;

COMMENT ON FUNCTION public.fn_check_valid_func_loc_path IS 'Variation of fn_is_valid_func_loc_path, that is suitable fro CHECK constraint. It will only check if the path > 1 segment and only up to n-1 segments.';

CREATE FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    sub_classes  text[];
    stmt text;
    num int;
    total int;
BEGIN
    num := 0;
    total := 0;
    sub_classes := 	ARRAY(SELECT code FROM assettype);
    FOR clss IN 1..array_upper(sub_classes,1)
        LOOP
            stmt := format('SELECT count(asset_id) FROM asset.a_tp_%s WHERE asset_id=''%s''::UUID', sub_classes[clss], the_asset);
            EXECUTE stmt into num;
            total := total + num;
        END LOOP;

    raise notice 'Number %', total;

END ; $$;

COMMENT ON FUNCTION public.fn_identify_multiple_subclasses(the_asset uuid) IS 'Identifies if the same asset has multiple subclasses e.g. same uuid in say envelope and facility. This is an invalid state but at the moment there is nothing in the database constraints that stops you from doing this.';



DROP INDEX asset_unaccent_name_idx;
DROP INDEX location_lower_idx;

DROP INDEX asset_asset_id_idx;


DROP MATERIALIZED VIEW dtpw.dtpw_core_report_view;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view_with_lpi;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view;
DROP VIEW IF EXISTS public.asset_core_view;
DROP VIEW IF EXISTS public.import_report_view;

alter table public.location alter column latitude type numeric(11,8);
alter table public.location alter column longitude type numeric(11,8);

CREATE OR REPLACE VIEW public.asset_core_view
 AS
 SELECT a.asset_id,
    a.asset_type_code AS asset_type,
    a.name,
    a.func_loc_path,
    a.deactivated_at IS NULL AS active,
    location.latitude,
    location.longitude,
    location.address,
    geoms.geom,
    identification.barcode,
    identification.serial_number
   FROM asset a
     LEFT JOIN location ON a.asset_id = location.asset_id
     LEFT JOIN geoms ON a.asset_id = geoms.asset_id
     LEFT JOIN asset_identification identification ON a.asset_id = identification.asset_id;

COMMENT ON VIEW public.asset_core_view IS 'Inner join the basic core tables indicating what it is and where it is';


-- View: dtpw.asset_core_dtpw_view
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
 AS
 SELECT core.asset_id,
    core.asset_type,
    core.name,
    core.func_loc_path,
    core.active,
    core.latitude,
    core.longitude,
    core.address,
    core.geom,
    core.barcode,
    core.serial_number,
    a_tp_e.district_code,
    a_tp_e.municipality_code,
    a_tp_e.town_code,
    a_tp_e.suburb_code,
    a_tp_f.facility_type_code,
    classification.responsible_dept_code,
    classification.is_owned,
    asset_link.external_id AS "EMIS"
   FROM asset_core_view core
     LEFT JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
     LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id
     LEFT JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
     LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id
     LEFT JOIN asset_classification classification ON core.asset_id = classification.asset_id
     LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id AND asset_link.external_id_type = (( SELECT external_id_type.type_id
           FROM external_id_type
          WHERE external_id_type.name::text = 'EMIS'::text));


COMMENT ON VIEW dtpw.asset_core_dtpw_view
    IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';

-- View: dtpw.asset_core_dtpw_view_with_lpi
CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi
 AS
 SELECT a.asset_id,
    a.asset_type,
    a.name,
    a.func_loc_path,
    a.active,
    a.latitude,
    a.longitude,
    a.address,
    a.geom,
    a.barcode,
    a.serial_number,
    a.district_code,
    a.municipality_code,
    a.town_code,
    a.suburb_code,
    a.facility_type_code,
    a.responsible_dept_code,
    a.is_owned,
    a."EMIS",
    p.lpi
   FROM dtpw.asset_core_dtpw_view a
     JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
     JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;

COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view ';

CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view AS
    SELECT asset_core_dtpw_view.asset_id,
          asset_core_dtpw_view.asset_type as asset_type_code,
          asset_core_dtpw_view.name,
          asset_core_dtpw_view.func_loc_path,
          asset_core_dtpw_view.active,
          asset_core_dtpw_view.latitude,
          asset_core_dtpw_view.longitude,
          asset_core_dtpw_view.address,
          asset_core_dtpw_view.barcode,
          asset_core_dtpw_view.serial_number,
          asset_core_dtpw_view.district_code,
          asset_core_dtpw_view.municipality_code,
          asset_core_dtpw_view.town_code,
          asset_core_dtpw_view.suburb_code,
          asset_core_dtpw_view.facility_type_code,
          asset_core_dtpw_view.responsible_dept_code,
          asset_core_dtpw_view.is_owned,
          asset_core_dtpw_view."EMIS",
          asset_core_dtpw_view.geom
   FROM dtpw.asset_core_dtpw_view
WITH DATA;


CREATE UNIQUE INDEX m1_asset_id_idx ON dtpw.dtpw_core_report_view USING btree (asset_id);
CREATE INDEX m1_func_loc_path_idx  ON dtpw.dtpw_core_report_view USING gist (func_loc_path);
CREATE INDEX m1_geom_idx  ON dtpw.dtpw_core_report_view USING gist (geom);
CREATE INDEX m1_district_code_idx ON dtpw.dtpw_core_report_view USING btree (district_code);
CREATE INDEX m1_municipality_code_idx ON dtpw.dtpw_core_report_view USING btree (municipality_code);
CREATE INDEX m1_suburb_code_idx ON dtpw.dtpw_core_report_view USING btree (suburb_code);
CREATE INDEX m1_town_code_idx ON dtpw.dtpw_core_report_view USING btree (town_code);
CREATE INDEX "m1_EMIS_idx" ON dtpw.dtpw_core_report_view USING btree ("EMIS");
CREATE INDEX m1_responsible_dept_code_idx ON dtpw.dtpw_core_report_view USING btree (responsible_dept_code);
CREATE INDEX m1_is_owned_idx ON dtpw.dtpw_core_report_view USING btree (is_owned);


COMMENT ON MATERIALIZED VIEW dtpw.dtpw_core_report_view IS 'This an example of a materialized view that flattens out the information in the core. It should be useful as the basis for many reports.';

REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;






CREATE OR REPLACE VIEW public.import_report_view
AS SELECT totals."Asset Type",
    totals."Total",
    location."Num with no location (lat/long)",
    geoms."Num with no geometry",
    classification."Num with no Responsible Department",
    location_address."Num with no address",
    identification_barcode."Num with no barcode",
    identification_serial_number."Num with no serial number"
   FROM ( SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'ENVELOPE'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'FACILITY'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'BUILDING'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'SITE'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'FLOOR'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'ROOM'::text
          GROUP BY asset.asset_type_code
        UNION
         SELECT asset.asset_type_code AS "Asset Type",
            count(asset.asset_id) AS "Total"
           FROM asset
          WHERE asset.asset_type_code::text = 'LANDPARCEL'::text
          GROUP BY asset.asset_type_code) totals
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no location (lat/long)"
           FROM asset a
             LEFT JOIN location l ON a.asset_id = l.asset_id
          WHERE l.asset_id IS NULL AND l.latitude IS NULL OR l.longitude IS NULL
          GROUP BY a.asset_type_code) location ON totals."Asset Type"::text = location."Asset Type"::text
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no address"
           FROM asset a
             LEFT JOIN location l ON a.asset_id = l.asset_id
          WHERE l.asset_id IS NULL OR l.address IS NULL
          GROUP BY a.asset_type_code) location_address ON totals."Asset Type"::text = location_address."Asset Type"::text
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no geometry"
           FROM asset a
             LEFT JOIN geoms g ON a.asset_id = g.asset_id
          WHERE g.asset_id IS NULL
          GROUP BY a.asset_type_code) geoms ON totals."Asset Type"::text = geoms."Asset Type"::text
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no Responsible Department"
           FROM asset a
             LEFT JOIN asset_classification c ON a.asset_id = c.asset_id
          WHERE c.asset_id IS NULL OR c.responsible_dept_code IS NULL
          GROUP BY a.asset_type_code) classification ON totals."Asset Type"::text = classification."Asset Type"::text
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no barcode"
           FROM asset a
             LEFT JOIN asset_identification i ON a.asset_id = i.asset_id
          WHERE i.asset_id IS NULL OR i.barcode IS NULL
          GROUP BY a.asset_type_code) identification_barcode ON totals."Asset Type"::text = identification_barcode."Asset Type"::text
     JOIN ( SELECT a.asset_type_code AS "Asset Type",
            count(a.asset_id) AS "Num with no serial number"
           FROM asset a
             LEFT JOIN asset_identification i ON a.asset_id = i.asset_id
          WHERE i.asset_id IS NULL OR i.serial_number IS NULL
          GROUP BY a.asset_type_code) identification_serial_number ON totals."Asset Type"::text = identification_serial_number."Asset Type"::text
  ORDER BY totals."Asset Type";

 comment on view public.import_report_view is 'A view that shows the number of entities of each type that was IMPORTED as well as indication of how many attribute values are missing.';


















INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0000000000-0', 'frankvr', 'changelog_public.json', NOW(), 1, '7:98b1b85a2397d3bd31314ca8a57dcd7e', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-3', 'frankvr (generated)', 'changelog_public.json', NOW(), 2, '7:16d2c8e11b850a0c5419a94c55126f5b', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-4', 'frankvr (generated)', 'changelog_public.json', NOW(), 3, '7:7b37ff112eb9e2b41fce27fbd5a381d2', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-5', 'frankvr (generated)', 'changelog_public.json', NOW(), 4, '7:5e17bb59eaac739bc0adebba28bab3f5', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-7', 'frankvr (generated)', 'changelog_public.json', NOW(), 5, '7:29cdd399135dc03ef1b17dfcb9f840ea', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-8', 'frankvr (generated)', 'changelog_public.json', NOW(), 6, '7:93c758d18caec3ce997dcf71e0bff261', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-9', 'frankvr (generated)', 'changelog_public.json', NOW(), 7, '7:e9da45aa9330126822abf23bb1e47fbd', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-10', 'frankvr (generated)', 'changelog_public.json', NOW(), 8, '7:bbb2a12b4a7370ba29933d03e8201996', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-11', 'frankvr (generated)', 'changelog_public.json', NOW(), 9, '7:c7a387e7fcf14b4c1d31055ccfa1532b', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-12', 'frankvr (generated)', 'changelog_public.json', NOW(), 10, '7:7a91e53be099d3c0ca8bfa2b1bacedbc', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-13', 'frankvr (generated)', 'changelog_public.json', NOW(), 11, '7:f639dd0c239086e44ca78204bf7ec561', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-15', 'frankvr (generated)', 'changelog_public.json', NOW(), 12, '7:fa038c973d3a7bd2cb1c0d6c83f4c007', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-16', 'frankvr (generated)', 'changelog_public.json', NOW(), 13, '7:ee2b42623aec99d93ab88e8bc946df5d', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-17', 'frankvr (generated)', 'changelog_public.json', NOW(), 14, '7:0de206c6f61abe9e4249b2f956562883', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-19', 'frankvr (generated)', 'changelog_public.json', NOW(), 15, '7:aa9881c6b62f2d68368e5e13411ff295', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-20', 'frankvr (generated)', 'changelog_public.json', NOW(), 16, '7:bd8a759e666c193bb7b1211bc3034360', 'addPrimaryKey, createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-21', 'frankvr (generated)', 'changelog_public.json', NOW(), 17, '7:ed4a570ee8065c120005b6f00b4872cf', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-22', 'frankvr (generated)', 'changelog_public.json', NOW(), 18, '7:7c3645983dad49c013ad90bb5758e454', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-23', 'frankvr (generated)', 'changelog_public.json', NOW(), 19, '7:5010fa8c1913f11061d49d962cb3ed01', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-24', 'frankvr (generated)', 'changelog_public.json', NOW(), 20, '7:9617d14b37180b553c616f891fd38314', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-25', 'frankvr (generated)', 'changelog_public.json', NOW(), 21, '7:3263994a36ee1534a96c5df2477fb59b', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-26', 'frankvr (generated)', 'changelog_public.json', NOW(), 22, '7:fbe71afc68ad1f74ab2e90a1055f0fd0', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-27', 'frankvr (generated)', 'changelog_public.json', NOW(), 23, '7:5d4096b2e1181cd69d04404ef34b81ba', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-28', 'frankvr (generated)', 'changelog_public.json', NOW(), 24, '7:4b16b9701e8a43a821320087424f256b', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-30', 'frankvr (generated)', 'changelog_public.json', NOW(), 25, '7:97f482417410dd839875b833c5cf5124', 'addPrimaryKey', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-31', 'frankvr (generated)', 'changelog_public.json', NOW(), 26, '7:5ba96745e415e0ebe4d5c0a9e8296f78', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-35', 'frankvr (generated)', 'changelog_public.json', NOW(), 27, '7:7329e4e39e08fe7ab7769dff093612df', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-36', 'frankvr (generated)', 'changelog_public.json', NOW(), 28, '7:9bdbe425ecb3b646d91c437ee8c51821', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-37', 'frankvr (generated)', 'changelog_public.json', NOW(), 29, '7:92a8f70c2c3a40049c690d4f16fc727a', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-38', 'frankvr (generated)', 'changelog_public.json', NOW(), 30, '7:70d1c99355c5700d8a5f46ae8cbc5919', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-39', 'frankvr (generated)', 'changelog_public.json', NOW(), 31, '7:d808516594fc42421023f0da0c7e870b', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-40', 'frankvr (generated)', 'changelog_public.json', NOW(), 32, '7:7802a61ecd6b579eb54b252b71825966', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-41', 'frankvr (generated)', 'changelog_public.json', NOW(), 33, '7:9362add1c80b44903b54090c6be7b462', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-42', 'frankvr (generated)', 'changelog_public.json', NOW(), 34, '7:4208d74e99513d7c42e035338791fcd8', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-43', 'frankvr (generated)', 'changelog_public.json', NOW(), 35, '7:5c2e3935217f767c6ca939e33b97cd8e', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-44', 'frankvr (generated)', 'changelog_public.json', NOW(), 36, '7:a2bec93c4947b34ad639b9a157e33a91', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-45', 'frankvr (generated)', 'changelog_public.json', NOW(), 37, '7:f2b9a26b0cb52e7046806e7fe5cbbaaf', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-46', 'frankvr (generated)', 'changelog_public.json', NOW(), 38, '7:357089fc5bb69df5a4acda44dd7a12aa', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-47', 'frankvr (generated)', 'changelog_public.json', NOW(), 39, '7:10d101b240b7e796c0e129b95b2d4c51', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('bc4cdbb488b32c229df99217e16a71906577f66f-1', 'fritzonfire', 'changelog_public.json', NOW(), 40, '7:81d4563705c744ba68b7ee3869b13de8', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-48', 'frankvr (generated)', 'changelog_public.json', NOW(), 41, '7:d42346f5b62859d2812dd33494a7e857', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-50', 'frankvr (generated)', 'changelog_public.json', NOW(), 42, '7:b5dbd4b9352778f158f52167304c5c3c', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-51', 'frankvr (generated)', 'changelog_public.json', NOW(), 43, '7:dba55a681b8008a0c065db45de6f7a95', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-52', 'frankvr (generated)', 'changelog_public.json', NOW(), 44, '7:618af9228d9ac663796b9aa515f1901d', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-53', 'frankvr (generated)', 'changelog_public.json', NOW(), 45, '7:dde337c696912fe2726d8a2dc96b1479', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1581943138671-54', 'frankvr (generated)', 'changelog_public.json', NOW(), 46, '7:887e94ecf8749cee679eaa9f23813d1c', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0000000000-1', 'frankvr', 'changelog_public.json', NOW(), 47, '7:6448ed5d210fcedcdc72742cef7224a1', 'sqlFile (x2)', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('c55ef5c4-6b58-41e6-8134-61d1c1ae225b-1', 'frankvr', 'changelog_public.json', NOW(), 48, '7:12768b5b4e2c6451175e7680cf81e246', 'createTable', 'Add support for tags', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('c55ef5c4-6b58-41e6-8134-61d1c1ae225b-2', 'frankvr', 'changelog_public.json', NOW(), 49, '7:67672169b167dc3fcd44d4dbcfc22f83', 'addForeignKeyConstraint', 'Add support for tags', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('c55ef5c4-6b58-41e6-8134-61d1c1ae225b-3', 'frankvr', 'changelog_public.json', NOW(), 50, '7:17477af9273ee76f3f268d0e5486aea1', 'createTable, createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0000000000-1000000', 'frankvr', 'changelog_public.json', NOW(), 51, '7:55e2ee38349544900ac19c6160fdc50f', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('70772cde-c594-4937-97af-5caf00950a46', 'frankvr', 'changelog_public.json', NOW(), 52, '7:09222f19b70793bd962a914e92041010', 'sqlFile', 'Load units', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('c55ef5c4-6b58-41e6-8134-61d1c1ae225b-4', 'frankvr', 'changelog_public.json', NOW(), 53, '7:96e1cb6381dfd1b125088e2dd6a4079a', 'sqlFile', 'Add support for tags', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('c127a622-b465-4ec4-93d8-a34ead141099', 'frankvr', 'changelog_public.json', NOW(), 54, '7:996c3e00e092c577a60d0bb54bab9689', 'sqlFile', 'Add standard views', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('4090ac4b-735c-4ea7-adb8-d2082cddc365', 'frankvr', 'changelog_public.json', NOW(), 55, '7:5812af58c87db3dc6199ae49696db95b', 'sqlFile', 'Modtrack', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('cc7605ab-95ee-49b6-8ec8-ee83b302f8e7', 'frankvr', 'changelog_public.json', NOW(), 56, '7:f50942e989e3a2b75b0e63eb2b9ba48c', 'sqlFile', 'Modtrack', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('e10ba961-b491-430a-af08-f56609ad9359', 'frankvr', 'changelog_public.json', NOW(), 57, '7:c8f6f909f0d939e65766b444d4cbefd9', 'sqlFile', 'Diacritics', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('348d2f83-7963-4892-a1e5-35868df44e5e', 'frankvr (generated)', 'changelog_public.json', NOW(), 58, '7:596c943fd56a29a01cf7fb7a4a307b14', 'createIndex (x2)', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-1', 'frankvr (generated)', 'changelog_audit.json', NOW(), 1, '7:a25ee9c9470b255e29d19e1cb483e4cf', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-2', 'frankvr (generated)', 'changelog_audit.json', NOW(), 2, '7:801bcb0d718e6af684f5dba39cf72d45', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-3', 'frankvr (generated)', 'changelog_audit.json', NOW(), 3, '7:312a738cca24a8f531a873a07f46dc95', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-4', 'frankvr (generated)', 'changelog_audit.json', NOW(), 4, '7:48288eda057de0b7edea8ef7c7030525', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-5', 'frankvr (generated)', 'changelog_audit.json', NOW(), 5, '7:58b540e6c386d9ab27d7a4623d59a68b', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-6', 'frankvr (generated)', 'changelog_audit.json', NOW(), 6, '7:6c9b310564720d1dde9a87105ed29b5d', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-7', 'frankvr (generated)', 'changelog_audit.json', NOW(), 7, '7:5731806e9c5c5542282ebbba7eaf0031', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395114168-8', 'frankvr (generated)', 'changelog_audit.json', NOW(), 8, '7:55ba21a0a6cef857cd3e04fb42296c82', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('f42927a2-b883-4702-b781-1032fff075a4', 'frankvr', 'changelog_audit.json', NOW(), 9, '7:a0fb8cefc97f981ea90031c6d4215c0e', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('ae2cd1ab-78b8-4fda-8577-58cf76ce705d', 'frankvr', 'changelog_audit.json', NOW(), 10, '7:7e6f1f5d13804eed852bdc9097a9da10', 'dropForeignKeyConstraint, renameColumn, createTable, createIndex, addForeignKeyConstraint', 'Rename asset to entity and change management of audit types', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('136e1f96-4e14-4a0e-be0f-864ed6c31f72', 'frankvr', 'changelog_audit.json', NOW(), 11, '7:281e8df8ccf56550c4fc5dba545a5b45', 'sqlFile', 'Audit types', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-1', 'frankvr (generated)', 'changelog_asset.json', NOW(), 1, '7:fc1c0dc2a739db6df39f656059addfc2', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-2', 'frankvr (generated)', 'changelog_asset.json', NOW(), 2, '7:324c670e0f46a7df9bd54afc251d262f', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-9', 'frankvr (generated)', 'changelog_asset.json', NOW(), 3, '7:09421c869912422de9b46609d0a1ea56', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-10', 'frankvr (generated)', 'changelog_asset.json', NOW(), 4, '7:6150bc352ba26625770b497b4b5ad89e', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-12', 'frankvr (generated)', 'changelog_asset.json', NOW(), 5, '7:2447e8117faee1093150df53edcdf7c8', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-13', 'frankvr (generated)', 'changelog_asset.json', NOW(), 6, '7:b4ca9a366c46f54e8cc9e710cce86a2a', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-14', 'frankvr (generated)', 'changelog_asset.json', NOW(), 7, '7:4b7d56193d59859536c89d8b655ad13b', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-15', 'frankvr (generated)', 'changelog_asset.json', NOW(), 8, '7:7a1465a41bbe9f82395f8271d34cbf18', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-16', 'frankvr (generated)', 'changelog_asset.json', NOW(), 9, '7:b5db3741c9fdd6b55106864fd40580b6', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-26', 'frankvr (generated)', 'changelog_asset.json', NOW(), 10, '7:eeb0ada05a1c5f6b9eabb9aa33f0ae73', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-5', 'frankvr (generated)', 'changelog_asset.json', NOW(), 11, '7:16fdbbc64df1be3b9453f46119e7c838', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-8', 'frankvr (generated)', 'changelog_asset.json', NOW(), 12, '7:dfac97d45a9281f380b380956c0e4e92', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-11', 'frankvr (generated)', 'changelog_asset.json', NOW(), 13, '7:6297d279ea9f7f1cae5bc6a5e69864d0', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-6', 'frankvr (generated)', 'changelog_asset.json', NOW(), 14, '7:d591b268c03712ebf24200b778571ba3', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-7', 'frankvr (generated)', 'changelog_asset.json', NOW(), 15, '7:14c14de81de77624c213464a26010ded', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-3', 'frankvr (generated)', 'changelog_asset.json', NOW(), 16, '7:ab7b902426559ba2d298905f2c78b39f', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-4', 'frankvr (generated)', 'changelog_asset.json', NOW(), 17, '7:b8c63b9a9237a132bf14f48879df44e6', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-27', 'frankvr (generated)', 'changelog_asset.json', NOW(), 18, '7:c2d5b9befe0f0b338912cff850d7eb89', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-29', 'frankvr (generated)', 'changelog_asset.json', NOW(), 19, '7:832158f98919803c65fcc9130fd465ac', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-30', 'frankvr (generated)', 'changelog_asset.json', NOW(), 20, '7:7c1ce3cfe82954f31ddbff4c558982e5', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-31', 'frankvr (generated)', 'changelog_asset.json', NOW(), 21, '7:e4dfb27de5e04ec3b7155fd20994d1c2', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-32', 'frankvr (generated)', 'changelog_asset.json', NOW(), 22, '7:0f9668fb9ca5788c0e8941ce7c2c2161', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-33', 'frankvr (generated)', 'changelog_asset.json', NOW(), 23, '7:6cd9821ebb882b9c83700f1b1c352f99', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-34', 'frankvr (generated)', 'changelog_asset.json', NOW(), 24, '7:83fdbe1fff9f7c558de20a5352b24733', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-36', 'frankvr (generated)', 'changelog_asset.json', NOW(), 25, '7:801c2ebf2619fb093542f17b575f6fd1', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-37', 'frankvr (generated)', 'changelog_asset.json', NOW(), 26, '7:7b6160c7972eb121e192417bc9b03aac', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-44', 'frankvr (generated)', 'changelog_asset.json', NOW(), 27, '7:3f571510dea45cc88f619bc6bcfa0d31', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-19', 'frankvr (generated)', 'changelog_asset.json', NOW(), 28, '7:e88afd7e5db1ae1b67924f121d466e79', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-20', 'frankvr (generated)', 'changelog_asset.json', NOW(), 29, '7:0e08f00aff995dfb69edb9b2a722f52e', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-24', 'frankvr (generated)', 'changelog_asset.json', NOW(), 30, '7:96e105becbaab7640581ae6aad15bcbf', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-25', 'frankvr (generated)', 'changelog_asset.json', NOW(), 31, '7:d0f198af27bd3bf89321cc9938429caf', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-28', 'frankvr (generated)', 'changelog_asset.json', NOW(), 32, '7:97a8222e82c9fca36bea6d36f6a85992', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-21', 'frankvr (generated)', 'changelog_asset.json', NOW(), 33, '7:4053cd7a2c1ca4050e870204eda1b3e3', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-22', 'frankvr (generated)', 'changelog_asset.json', NOW(), 34, '7:8b39f3eef58fe9b1f6ddb34f0412b456', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-23', 'frankvr (generated)', 'changelog_asset.json', NOW(), 35, '7:e43e0ab3df909c1effd097787fd43168', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-17', 'frankvr (generated)', 'changelog_asset.json', NOW(), 36, '7:378c2a9404ad0b6db2b390a4e5acf071', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-18', 'frankvr (generated)', 'changelog_asset.json', NOW(), 37, '7:001e1a08ec637c6af92bb59192bc527e', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-35', 'frankvr (generated)', 'changelog_asset.json', NOW(), 38, '7:217eb6bf5f151f7947fc0e64e5357a8e', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-38', 'frankvr (generated)', 'changelog_asset.json', NOW(), 39, '7:6a28accc054f55bccc8f929578f9082f', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-39', 'frankvr (generated)', 'changelog_asset.json', NOW(), 40, '7:768311a48ef3a0b8ffe0375f07644c0f', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-40', 'frankvr (generated)', 'changelog_asset.json', NOW(), 41, '7:401ababf354e84024f98e34e227f1c46', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-41', 'frankvr (generated)', 'changelog_asset.json', NOW(), 42, '7:cdbda1261f6717152c6a712d5e1999c1', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-42', 'frankvr (generated)', 'changelog_asset.json', NOW(), 43, '7:036800352f532da264c24a32b84d2879', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403835611-43', 'frankvr (generated)', 'changelog_asset.json', NOW(), 44, '7:743317a233ba5a7f39577f5674ecad78', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('510fc302-4d6e-404d-8cd5-a3b9911da02a', 'frankvr', 'changelog_asset.json', NOW(), 45, '7:7c53755375ebbf7145f768bb12b9151f', 'sqlFile', 'Add standard views', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('f8f5de0f-e360-458a-af11-336e59d279f7', 'frankvr', 'changelog_asset.json', NOW(), 46, '7:4f9004629441c90f4b20b0e2126e87ca', 'sqlFile (x2)', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('138885db-0c73-4a85-b9af-d68fb23e1976', 'frankvr', 'changelog_asset.json', NOW(), 47, '7:a1517691b97f62cdff1c4113241e02d7', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-1', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 1, '7:c434a82cdb9d3da1543b6711e899c808', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-2', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 2, '7:c1bfd89d2c65310bca27ddb2badb97de', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-3', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 3, '7:df9e44e0f606ec79695eeeafe61e1e47', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-4', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 4, '7:66afb416b8aa1976301533cfea9c2bbb', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-5', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 5, '7:fe973f6fa8e1266fe5fe2ca3f77180a8', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-6', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 6, '7:3b6ef3b772cae0f313dc580d01b3cc78', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-7', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 7, '7:b8c714a3abd092003589510bbfa2220e', 'addUniqueConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-8', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 8, '7:10d5b397082d728db14b7f97435b1446', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583395109147-9', 'frankvr (generated)', 'changelog_access_control.json', NOW(), 9, '7:b8dc5d40a27ac414292eaa4780c881eb', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('a1725304-b4a6-4bbe-8e55-05b74086471a', 'frankvr', 'changelog_access_control.json', NOW(), 10, '7:7812157546ffe41a73b0ac473ec216a5', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('a1725304-b4a6-4bbe-8e55-05b74086471a-1', 'frankvr', 'changelog_access_control.json', NOW(), 11, '7:8074abed5a584a9bebcb5fbf5914f073', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-1', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 1, '7:2af3d517b111ae89f7e521efa624763a', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-2', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 2, '7:3d2bd1ade69b8a86f13ddd390e5b5a2f', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-3', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 3, '7:292baf533827bf92fa92beafd223e682', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-4', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 4, '7:52f7438e81d9037ab22a96030f034ac3', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-5', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 5, '7:8d82d7111f7283f3438a2bebbdba2cc3', 'createTable', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-6', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 6, '7:0d07c5fb5e989601d0292ca1abbacb9c', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-7', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 7, '7:5d410cfc314ae56242af4b7c46f1138b', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-8', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 8, '7:f88f2fef1d7c67bb7e1c199dd1814e99', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-9', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 9, '7:846ca40aeeb8a14239787c92d28fae39', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-10', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 10, '7:1cb9040eef919b653654d9777bfb1a7c', 'createIndex', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('1583403225361-11', 'frankvr (generated)', 'changelog_dtpw.json', NOW(), 11, '7:3f5af5c638f2d3f936cde4a18dd2a7bc', 'addForeignKeyConstraint', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0000000000-1', 'frankvr', 'changelog_dtpw.json', NOW(), 12, '7:7e2803d9674a79c9683ef5f6c4bd900e', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0000000000-2', 'frankvr', 'changelog_dtpw.json', NOW(), 13, '7:5c6e18cc88d84d1cf4f62b77cd7a3c53', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('0bd5c466-9743-4b85-a10b-c12e50039273', 'frankvr', 'changelog_dtpw.json', NOW(), 14, '7:2400a93eaa0fb20dc329cd4cdca7c9b2', 'sqlFile', 'Add support for tags', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('8ed152b2-f8c7-403e-a57e-737ee5f370dc', 'frankvr', 'changelog_dtpw.json', NOW(), 15, '7:b52f535e44040b8c9ed55a749a46e7bc', 'sqlFile', 'Add support for tags', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('efefd1e6-3a5b-4f01-be56-1146a1c46556', 'frankvr', 'changelog_dtpw.json', NOW(), 16, '7:12aedf3b06b3fac8ef29c387ee45e1ad', 'addForeignKeyConstraint (x2)', 'Add missing FKs', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('668ca1e7-bfb5-4b7e-afad-513cd6516538', 'frankvr', 'changelog_dtpw.json', NOW(), 17, '7:1d1524924cd7cec1cb7439da7db9ca9a', 'sqlFile (x3)', 'Apply baked in db users', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('8e9d76f9-91fa-44bb-b9c6-c86f76d7484f', 'frankvr', 'changelog_dtpw.json', NOW(), 18, '7:3616eb67bd18514d18270df25b404d60', 'sqlFile', '', 'EXECUTED', NULL, NULL, '3.4.1');

INSERT INTO public.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('a3606bec-1fa8-4183-871e-d52ec90aecc4', 'frankvr', 'changelog_dtpw.json', NOW(), 19, '7:b52f535e44040b8c9ed55a749a46e7bc', 'sqlFile', 'Apply view changes for versioning and bug fixing', 'EXECUTED', NULL, NULL, '3.4.1');


------------------------

--ALTER TABLE public.asset ADD CONSTRAINT asset_check CHECK ((public.fn_check_valid_func_loc_path(func_loc_path) = true));