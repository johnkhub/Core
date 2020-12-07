DROP INDEX IF EXISTS asset_func_loc_path_idx;
CREATE INDEX IF NOT EXISTS asset_func_loc_path_idx ON public.asset USING gist (func_loc_path);

DROP INDEX IF EXISTS asset_adm_path_idx;
CREATE INDEX IF NOT EXISTS asset_adm_path_idx ON public.asset USING gist (adm_path);

DROP INDEX IF EXISTS asset_grap_path_idx;
CREATE INDEX IF NOT EXISTS asset_grap_path_idx ON public.asset USING gist (grap_path);

DROP INDEX IF EXISTS dtpw_organogram_path_idx;
CREATE INDEX IF NOT EXISTS dtpw_organogram_path_idx ON dtpw.ei_district_link USING gist (organogram_path);


DELETE FROM unit WHERE code = 'area_m';
INSERT INTO unit (code,name,is_si,symbol,type) VALUES ('area_m2', 'Square Meter', true, '㎡', 'T_AREA') ON CONFLICT(code) DO NOTHING;

ALTER TABLE public.kv_type ALTER COLUMN code SET DATA TYPE varchar(30);

-- Table: dtpw.ref_accommodation_type

-- DROP TABLE dtpw.ref_accommodation_type;

CREATE TABLE dtpw.ref_accommodation_type
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_accommodation_type_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE dtpw.ref_accommodation_type
    OWNER to postgres;
-- Index: ref_accommodation_type_k_idx

-- DROP INDEX dtpw.ref_accommodation_type_k_idx;

CREATE UNIQUE INDEX ref_accommodation_type_k_idx
    ON dtpw.ref_accommodation_type USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_accommodation_type_v_idx

-- DROP INDEX dtpw.ref_accommodation_type_v_idx;

CREATE UNIQUE INDEX ref_accommodation_type_v_idx
    ON dtpw.ref_accommodation_type USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: dtpw.ref_land_use_class

-- DROP TABLE dtpw.ref_land_use_class;

CREATE TABLE dtpw.ref_land_use_class
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_land_use_class_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE dtpw.ref_land_use_class
    OWNER to postgres;
-- Index: ref_land_use_class_k_idx

-- DROP INDEX dtpw.ref_land_use_class_k_idx;

CREATE UNIQUE INDEX ref_land_use_class_k_idx
    ON dtpw.ref_land_use_class USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_land_use_class_v_idx

-- DROP INDEX dtpw.ref_land_use_class_v_idx;

CREATE UNIQUE INDEX ref_land_use_class_v_idx
    ON dtpw.ref_land_use_class USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: public.ref_accessibility_rating

-- DROP TABLE public.ref_accessibility_rating;

CREATE TABLE public.ref_accessibility_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_accessibility_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_accessibility_rating
    OWNER to postgres;
-- Index: ref_accessibility_rating_k_idx

-- DROP INDEX public.ref_accessibility_rating_k_idx;

CREATE UNIQUE INDEX ref_accessibility_rating_k_idx
    ON public.ref_accessibility_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_accessibility_rating_v_idx

-- DROP INDEX public.ref_accessibility_rating_v_idx;

CREATE UNIQUE INDEX ref_accessibility_rating_v_idx
    ON public.ref_accessibility_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: public.ref_asset_class

-- DROP TABLE public.ref_asset_class;

CREATE TABLE public.ref_asset_class
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_asset_class_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_asset_class
    OWNER to postgres;
-- Index: ref_asset_class_k_idx

-- DROP INDEX public.ref_asset_class_k_idx;

CREATE UNIQUE INDEX ref_asset_class_k_idx
    ON public.ref_asset_class USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_asset_class_v_idx

-- DROP INDEX public.ref_asset_class_v_idx;

CREATE UNIQUE INDEX ref_asset_class_v_idx
    ON public.ref_asset_class USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: public.ref_asset_nature

-- DROP TABLE public.ref_asset_nature;

CREATE TABLE public.ref_asset_nature
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_asset_nature_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_asset_nature
    OWNER to postgres;
-- Index: ref_asset_nature_k_idx

-- DROP INDEX public.ref_asset_nature_k_idx;

CREATE UNIQUE INDEX ref_asset_nature_k_idx
    ON public.ref_asset_nature USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_asset_nature_v_idx

-- DROP INDEX public.ref_asset_nature_v_idx;

CREATE UNIQUE INDEX ref_asset_nature_v_idx
    ON public.ref_asset_nature USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: public.ref_condition_rating

-- DROP TABLE public.ref_condition_rating;

CREATE TABLE public.ref_condition_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    asset_info text COLLATE pg_catalog."default" NOT NULL,
    risk text NOT NULL,
    CONSTRAINT ref_condition_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_condition_rating
    OWNER to postgres;
-- Index: ref_condition_rating_k_idx

-- DROP INDEX public.ref_condition_rating_k_idx;

CREATE UNIQUE INDEX ref_condition_rating_k_idx
    ON public.ref_condition_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_condition_rating_v_idx

-- DROP INDEX public.ref_condition_rating_v_idx;

CREATE UNIQUE INDEX ref_condition_rating_v_idx
    ON public.ref_condition_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: public.ref_confidence_rating

-- DROP TABLE public.ref_confidence_rating;

CREATE TABLE public.ref_confidence_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    accuracy double precision NOT NULL,
    CONSTRAINT ref_confidence_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_confidence_rating
    OWNER to postgres;
-- Index: ref_confidence_rating_k_idx

-- DROP INDEX public.ref_confidence_rating_k_idx;

CREATE UNIQUE INDEX ref_confidence_rating_k_idx
    ON public.ref_confidence_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_confidence_rating_v_idx

-- DROP INDEX public.ref_confidence_rating_v_idx;

CREATE UNIQUE INDEX ref_confidence_rating_v_idx
    ON public.ref_confidence_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: public.ref_criticality_rating

-- DROP TABLE public.ref_criticality_rating;

CREATE TABLE public.ref_criticality_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_criticality_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_criticality_rating
    OWNER to postgres;
-- Index: ref_criticality_rating_k_idx

-- DROP INDEX public.ref_criticality_rating_k_idx;

CREATE UNIQUE INDEX ref_criticality_rating_k_idx
    ON public.ref_criticality_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_criticality_rating_v_idx

-- DROP INDEX public.ref_criticality_rating_v_idx;

CREATE UNIQUE INDEX ref_criticality_rating_v_idx
    ON public.ref_criticality_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Table: public.ref_performance_rating

-- DROP TABLE public.ref_performance_rating;

CREATE TABLE public.ref_performance_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    asset_info text COLLATE pg_catalog."default" NOT NULL
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_performance_rating
    OWNER to postgres;
-- Index: ref_performance_rating_k_idx

-- DROP INDEX public.ref_performance_rating_k_idx;

CREATE UNIQUE INDEX ref_performance_rating_k_idx
    ON public.ref_performance_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_performance_rating_v_idx

-- DROP INDEX public.ref_performance_rating_v_idx;

CREATE UNIQUE INDEX ref_performance_rating_v_idx
    ON public.ref_performance_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: public.ref_utilisation_rating

-- DROP TABLE public.ref_utilisation_rating;

CREATE TABLE public.ref_utilisation_rating
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_utilisation_rating_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.ref_utilisation_rating
    OWNER to postgres;
-- Index: ref_utilisation_rating_k_idx

-- DROP INDEX public.ref_utilisation_rating_k_idx;

CREATE UNIQUE INDEX ref_utilisation_rating_k_idx
    ON public.ref_utilisation_rating USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_utilisation_rating_v_idx

-- DROP INDEX public.ref_utilisation_rating_v_idx;

CREATE UNIQUE INDEX ref_utilisation_rating_v_idx
    ON public.ref_utilisation_rating USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


-- Table: dtpw.ref_deed_office

-- DROP TABLE dtpw.ref_deed_office;

CREATE TABLE dtpw.ref_deed_office
(
    k character varying(10) COLLATE pg_catalog."default" NOT NULL,
    v character varying COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT now(),
    deactivated_at timestamp without time zone,
    allow_delete boolean DEFAULT false,
    activated_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT ref_deed_office_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE dtpw.ref_deed_office
    OWNER to postgres;
-- Index: ref_deed_office_k_idx

-- DROP INDEX dtpw.ref_deed_office_k_idx;

CREATE UNIQUE INDEX ref_deed_office_k_idx
    ON dtpw.ref_deed_office USING btree
        (k COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ref_deed_office_v_idx

-- DROP INDEX dtpw.ref_deed_office_v_idx;

CREATE UNIQUE INDEX ref_deed_office_v_idx
    ON dtpw.ref_deed_office USING btree
        (v COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

--
-- Export view
--
DROP VIEW dtpw.dtpw_export_view;
CREATE OR REPLACE VIEW dtpw.dtpw_export_view AS
SELECT
    core.asset_id,
    core.asset_type_code,
    core.name,
    replace(ltree2text(core.func_loc_path),'.', '-') AS func_loc_path,
    core.active,
    core.latitude,
    core.longitude,
    core.address,
    core.barcode,
    core.serial_number,
    core.district_code,
    core.municipality_code,
    core.town_code,
    core.suburb_code,

    core.facility_type_code,
    core.lpi,

    core.responsible_dept_code,
    core.is_owned,

    ei."EMIS",
    ei.ei_district_code,

    tags.tags,

    quantity.num_units,
    quantity.unit_code,

    ST_asText(core.geom) AS geom
FROM
    dtpw.asset_core_dtpw_view_with_lpi core
        LEFT JOIN dtpw.asset_core_dtpw_ei_view ei ON core.asset_id = ei.asset_id
        LEFT JOIN public.asset_tags tags ON core.asset_id = tags.asset_id
        LEFT JOIN public.quantity quantity ON core.asset_id = quantity.asset_id AND quantity.name = 'extent';

COMMENT ON VIEW dtpw.dtpw_export_view IS 'Converts the geometry to well-known text and provides all asset rows';




DO
$do$
    BEGIN
        IF NOT EXISTS (
                SELECT *
                FROM   pg_catalog.pg_roles
                WHERE  rolname = 'report_reader') THEN

            CREATE ROLE report_reader LOGIN PASSWORD 'report_reader';
        END IF;
    END
$do$;


GRANT USAGE ON SCHEMA dtpw TO report_reader;


GRANT SELECT ON  dtpw.dtpw_core_report_view TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_view_with_lpi  TO report_reader;

GRANT SELECT ON  dtpw.asset_core_dtpw_ei_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_gi_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_hi_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_rnm_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_iam_view  TO report_reader;
GRANT SELECT ON  dtpw.asset_core_dtpw_ppp_view  TO report_reader;

GRANT SELECT ON  dtpw.dtpw_core_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_ei_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_gi_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_hi_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_iam_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_rnm_report_view_wrapper  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_ppp_report_view_wrapper  TO report_reader;

GRANT SELECT ON  dtpw.dtpw_export_view  TO report_reader;
GRANT SELECT ON  dtpw.dtpw_export_view  TO normal_reader;

--
-- access control
--
GRANT USAGE ON SCHEMA access_control TO report_reader;

GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_access TO report_reader;
GRANT EXECUTE ON FUNCTION access_control.fn_get_effective_grant TO report_reader;



INSERT INTO kv_type (code,name,"table") VALUES ('ASSET_CLASS', 'Asset Class', 'public.ref_asset_class') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('ASSET_NATURE', 'Asset Nature', 'public.ref_asset_nature') ON CONFLICT (code) DO NOTHING;

INSERT INTO kv_type (code,name,"table") VALUES ('CRITICALITY_RATING', 'Criticality Rating', 'public.ref_criticality_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('UTILISATION_RATING', 'Utilisation Rating', 'public.ref_utilisation_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('ACCESSIBILITY_RATING', 'Accessibility Rating', 'public.ref_accessibility_rating') ON CONFLICT (code) DO NOTHING;

-- Non-standard KV
INSERT INTO kv_type (code,name,"table") VALUES ('CONFIDENCE_RATING', 'Data Confidence Rating', 'public.ref_confidence_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('CONDITION_RATING', 'Condition Rating', 'public.ref_condition_rating') ON CONFLICT (code) DO NOTHING;
INSERT INTO kv_type (code,name,"table") VALUES ('PERFORMANCE_RATING', 'Performance Rating', 'public.ref_performance_rating') ON CONFLICT (code) DO NOTHING;