DROP INDEX IF EXISTS asset_classif_dept;

CREATE INDEX asset_classif_dept
    ON public.asset_classification USING btree
        (responsible_dept_code ASC NULLS LAST)
    TABLESPACE pg_default;

DROP INDEX IF EXISTS public.asset_classif_owned;

CREATE INDEX asset_classif_owned
    ON public.asset_classification USING btree
        (is_owned ASC NULLS LAST)
    TABLESPACE pg_default;

