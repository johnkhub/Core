DROP MATERIALIZED VIEW dtpw.dtpw_core_report_view;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view_with_lpi;
DROP VIEW IF EXISTS dtpw.asset_core_dtpw_view;
DROP VIEW IF EXISTS public.asset_core_view;

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

ALTER TABLE public.asset_core_view
    OWNER TO postgres;
COMMENT ON VIEW public.asset_core_view
    IS 'Inner join the basic core tables indicating what it is and where it is';


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

ALTER TABLE dtpw.asset_core_dtpw_view
    OWNER TO postgres;
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

ALTER TABLE dtpw.asset_core_dtpw_view_with_lpi
    OWNER TO postgres;
COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi
    IS 'Adds lpi to asset_core_dtpw_view ';


-- View: dtpw.dtpw_core_report_view
CREATE MATERIALIZED VIEW dtpw.dtpw_core_report_view
TABLESPACE pg_default
AS
 SELECT asset_core_dtpw_view.asset_id,
    asset_core_dtpw_view.asset_type,
    asset_core_dtpw_view.name,
    asset_core_dtpw_view.func_loc_path,
    asset_core_dtpw_view.active,
    asset_core_dtpw_view.latitude,
    asset_core_dtpw_view.longitude,
    asset_core_dtpw_view.address,
    asset_core_dtpw_view.geom,
    asset_core_dtpw_view.barcode,
    asset_core_dtpw_view.serial_number,
    asset_core_dtpw_view.district_code,
    asset_core_dtpw_view.municipality_code,
    asset_core_dtpw_view.town_code,
    asset_core_dtpw_view.suburb_code,
    asset_core_dtpw_view.facility_type_code,
    asset_core_dtpw_view.responsible_dept_code,
    asset_core_dtpw_view.is_owned,
    asset_core_dtpw_view."EMIS"
   FROM dtpw.asset_core_dtpw_view
WITH DATA;

ALTER TABLE dtpw.dtpw_core_report_view
    OWNER TO postgres;


CREATE INDEX "m1_EMIS_idx"
    ON dtpw.dtpw_core_report_view USING btree
    ("EMIS" COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE UNIQUE INDEX m1_asset_id_idx
    ON dtpw.dtpw_core_report_view USING btree
    (asset_id)
    TABLESPACE pg_default;
CREATE INDEX m1_district_code_idx
    ON dtpw.dtpw_core_report_view USING btree
    (district_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE INDEX m1_func_loc_path_idx
    ON dtpw.dtpw_core_report_view USING gist
    (func_loc_path)
    TABLESPACE pg_default;
CREATE INDEX m1_geom_idx
    ON dtpw.dtpw_core_report_view USING gist
    (geom)
    TABLESPACE pg_default;
CREATE INDEX m1_is_owned_idx
    ON dtpw.dtpw_core_report_view USING btree
    (is_owned)
    TABLESPACE pg_default;
CREATE INDEX m1_municipality_code_idx
    ON dtpw.dtpw_core_report_view USING btree
    (municipality_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE INDEX m1_responsible_dept_code_idx
    ON dtpw.dtpw_core_report_view USING btree
    (responsible_dept_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE INDEX m1_suburb_code_idx
    ON dtpw.dtpw_core_report_view USING btree
    (suburb_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE INDEX m1_town_code_idx
    ON dtpw.dtpw_core_report_view USING btree
    (town_code COLLATE pg_catalog."default")
    TABLESPACE pg_default;
	
refresh materialized view dtpw.dtpw_core_report_view;