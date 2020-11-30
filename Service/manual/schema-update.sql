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