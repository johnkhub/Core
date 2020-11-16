DROP TABLE IF EXISTS public.quantity CASCADE;
CREATE TABLE public.quantity
(
    asset_id uuid NOT NULL,
    unit_code character varying(10) COLLATE pg_catalog."default" NOT NULL,
    num_units numeric NOT NULL,
    name text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT quantity_pkey PRIMARY KEY (asset_id, name),
    CONSTRAINT asset_id_fkey FOREIGN KEY (asset_id)
        REFERENCES public.asset (asset_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT unit_code_fk FOREIGN KEY (unit_code)
        REFERENCES public.unit (code) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
    WITH (
        OIDS = FALSE
    )
    TABLESPACE pg_default;

ALTER TABLE public.quantity
    OWNER to postgres;

CREATE OR REPLACE VIEW public.quantities_view AS
SELECT
    q.name as name, q.num_units as num_units,
    u.code as unit_code, u.name as unit_name, u.is_si as is_si, u.symbol as symbol, u.type as unit_type
FROM
    public.quantity q
        JOIN unit u ON q.unit_code = u.code
;