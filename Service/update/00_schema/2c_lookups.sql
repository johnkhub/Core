-- IMPORT THE CSV FILES HERE

-- suburb
DELETE FROM asset.ref_suburb WHERE k IN ('WCP_605', 'WCP_838', 'WCP_1100', 'WCP_647', 'WCP_860', 'WCP_40', 'WCP_1396');

--ALTER TABLE asset.ref_suburb DROP CONSTRAINT fk_ward;
ALTER TABLE asset.ref_suburb
    ADD CONSTRAINT fk_ward FOREIGN KEY (ward_k)
        REFERENCES asset.ref_ward (k) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;


--ALTER TABLE asset.ref_suburb DROP CONSTRAINT fk_town;
ALTER TABLE asset.ref_suburb
    ADD CONSTRAINT fk_town FOREIGN KEY (town_k)
        REFERENCES asset.ref_town (k) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

--ALTER TABLE asset.ref_town DROP CONSTRAINT fk_local;
ALTER TABLE asset.ref_town
    ADD CONSTRAINT fk_local FOREIGN KEY (local_municipality_k)
        REFERENCES asset.ref_municipality (k) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;

--ALTER TABLE asset.ref_municipality DROP CONSTRAINT fk_district;
ALTER TABLE asset.ref_municipality
    ADD CONSTRAINT fk_district FOREIGN KEY (district_k)
        REFERENCES asset.ref_district (k) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;