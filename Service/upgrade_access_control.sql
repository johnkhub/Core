DROP TABLE asset_import CASCADE;
DROP TABLE land_parcel_import;
DROP TABLE modtrack_meta;
DROP TABLE modtrack_tables;
DROP TABLE migration_version;

DROP VIEW IF EXISTS import_report_view;
DROP VIEW IF EXISTS ar_lite_view;

CREATE TABLE public.asset_tags (
	asset_id uuid NOT NULL,
	tags _text NOT NULL,
	CONSTRAINT "PK_ASSET_TAGS" PRIMARY KEY (asset_id)
);
ALTER TABLE public.asset_tags ADD CONSTRAINT asset_tags_asset_id_fkey FOREIGN KEY (asset_id) REFERENCES asset(asset_id);

CREATE TABLE public.tags (
	k varchar(10) NOT NULL,
	v varchar NULL,
	creation_date timestamp NOT NULL DEFAULT now(),
	activated_at timestamp NULL,
	deactivated_at timestamp NULL,
	allow_delete bool NULL DEFAULT false,
	CONSTRAINT tags_k_check CHECK ((((k)::text <> ''::text) AND ((k)::text ~ '^[\w]*$'::text)))
);

CREATE UNIQUE INDEX tags_k_idx ON public.tags USING btree (k);


DROP VIEW IF EXISTS public.asset_core_view;
CREATE OR REPLACE VIEW public.asset_core_view
AS SELECT a.asset_id,
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



