CREATE TABLE public.grouping_id_type
(
    type_id uuid NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT grouping_id_type_pkey PRIMARY KEY (type_id)
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

COMMENT ON COLUMN public.grouping_id_type.type_id  IS 'EMIS';


CREATE TABLE public.asset_grouping
(
    asset_id uuid NOT NULL,
    grouping_id character varying COLLATE pg_catalog."default" NOT NULL,
    grouping_id_type uuid NOT NULL,
    CONSTRAINT asset_grouping_pkey PRIMARY KEY (asset_id, grouping_id),
    CONSTRAINT asset_grouping_asset_id_fkey FOREIGN KEY (asset_id)
        REFERENCES public.asset (asset_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT asset_grouping_grouping_id_type_fkey FOREIGN KEY (grouping_id_type)
        REFERENCES public.grouping_id_type (type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;



CREATE INDEX asset_grouping_grouping_id_type_asset_id_grouping_id_idx
    ON public.asset_grouping USING btree
        (grouping_id_type ASC NULLS LAST, asset_id ASC NULLS LAST, grouping_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


CREATE INDEX asset_grouping_grouping_id_type_grouping_id_idx
    ON public.asset_grouping USING btree
        (grouping_id_type ASC NULLS LAST, grouping_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;


DROP INDEX public.asset_link_external_id_type_asset_id_external_id_idx;
CREATE UNIQUE INDEX asset_link_external_id_type_asset_id_external_id_idx
    ON public.asset_link USING btree
        (external_id_type ASC NULLS LAST, asset_id ASC NULLS LAST, external_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

DROP INDEX public.asset_link_external_id_type_external_id_idx;

CREATE UNIQUE INDEX asset_link_external_id_type_external_id_idx
    ON public.asset_link USING btree
        (external_id_type ASC NULLS LAST, external_id COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;







ALTER TABLE public.asset_grouping OWNER to imqs;
GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE public.asset_grouping TO importer;
GRANT ALL ON TABLE public.asset_grouping TO imqs;
GRANT SELECT ON TABLE public.asset_grouping TO normal_reader;
GRANT INSERT, SELECT, UPDATE ON TABLE public.asset_grouping TO normal_writer;


ALTER TABLE public.grouping_id_type OWNER to imqs;
GRANT INSERT, SELECT, UPDATE ON TABLE public.grouping_id_type TO importer;
GRANT ALL ON TABLE public.grouping_id_type TO imqs;
GRANT SELECT ON TABLE public.grouping_id_type TO normal_reader;
GRANT INSERT, SELECT, UPDATE ON TABLE public.grouping_id_type TO normal_writer;