update asset.a_tp_landparcel set lpi = trim(lpi);

update asset.ref_district set v = trim(v);
update asset.ref_municipality set v = trim(v);
update asset.ref_region set v = trim(v);
update asset.ref_suburb set v = trim(v);
update asset.ref_town set v = trim(v);
update asset.ref_ward set v = trim(v);

update dtpw.ref_branch set v = trim(v);
update dtpw.ref_chief_directorate set v = trim(v);
update dtpw.ref_client_department set v = trim(v);

update public.asset set name = trim(name);
update public.asset_link set external_id = trim(external_id);
update public.location set address = trim(address);

refresh materialized view dtpw.dtpw_core_report_view;






