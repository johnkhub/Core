CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view
AS SELECT core.asset_id,
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
    asset_link.external_id AS "EMIS"
   FROM asset_core_view core
     JOIN asset e ON subpath(core.func_loc_path, 0, 1) = e.func_loc_path
     LEFT JOIN asset.a_tp_envelope a_tp_e ON e.asset_id = a_tp_e.asset_id
     JOIN asset f ON subpath(core.func_loc_path, 0, 2) = f.func_loc_path
     LEFT JOIN asset.a_tp_facility a_tp_f ON f.asset_id = a_tp_f.asset_id
     JOIN asset_classification classification ON core.asset_id = classification.asset_id
     LEFT JOIN asset_link ON core.asset_id = asset_link.asset_id AND asset_link.external_id_type = (( SELECT external_id_type.type_id
           FROM external_id_type
          WHERE external_id_type.name::text = 'EMIS'::text));


-- dtpw.asset_core_dtpw_view_with_lpi source

CREATE OR REPLACE VIEW dtpw.asset_core_dtpw_view_with_lpi
AS SELECT a.asset_id,
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
    a."EMIS",
    p.lpi
   FROM dtpw.asset_core_dtpw_view a
     JOIN asset.asset_landparcel l ON a.asset_id = l.asset_id
     JOIN asset.landparcel_view p ON l.landparcel_asset_id = p.asset_id;

COMMENT ON VIEW dtpw.asset_core_dtpw_view IS 'DTPW view. Joins facility and envelope information onto core information. Incorporates EMIS number and responsible department.';
COMMENT ON VIEW dtpw.asset_core_dtpw_view_with_lpi IS 'Adds lpi to asset_core_dtpw_view';

ALTER TABLE dtpw.ref_branch ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE dtpw.ref_chief_directorate ALTER COLUMN creation_date SET DEFAULT now();
ALTER TABLE dtpw.ref_client_department ALTER COLUMN creation_date SET DEFAULT now();

ALTER TABLE dtpw.ref_chief_directorate ADD CONSTRAINT chiefdir_branch_fkey FOREIGN KEY (branch_code) REFERENCES dtpw.ref_branch(k);
ALTER TABLE dtpw.ref_client_department ADD CONSTRAINT clientdep_chiefdir_fkey FOREIGN KEY (chief_directorate_code) REFERENCES dtpw.ref_chief_directorate(k);
