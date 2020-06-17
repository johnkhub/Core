DROP INDEX asset.ref_district_v_idx;

CREATE INDEX ref_district_v_idx
    ON asset.ref_district USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- add columns to parent entities
ALTER TABLE asset.ref_ward ADD COLUMN local_municipality_k varchar(10);
ALTER TABLE asset.ref_suburb ADD COLUMN town_k varchar(10);
ALTER TABLE asset.ref_suburb ADD COLUMN ward_k varchar(10);
ALTER TABLE asset.ref_town ADD COLUMN local_municipality_k varchar(10);
ALTER TABLE asset.ref_municipality ADD COLUMN district_k varchar(10);

-- add geometry
ALTER TABLE asset.ref_ward ADD COLUMN geom geometry;
ALTER TABLE asset.ref_suburb ADD COLUMN geom geometry;
ALTER TABLE asset.ref_town ADD COLUMN geom geometry;
ALTER TABLE asset.ref_municipality ADD geom geometry;
ALTER TABLE asset.ref_region ADD COLUMN geom geometry;
ALTER TABLE asset.ref_district ADD COLUMN geom geometry;

