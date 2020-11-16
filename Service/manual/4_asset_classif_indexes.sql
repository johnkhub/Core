DROP INDEX public.func_loc_path_idx;

CREATE INDEX asset_classif_dept
    ON public.asset_classification USING btree
        (responsible_dept_code ASC NULLS LAST)
    TABLESPACE pg_default;

DROP INDEX public.asset_classif_owned;

CREATE INDEX asset_classif_owned
    ON public.asset_classification USING btree
        (is_owned ASC NULLS LAST)
    TABLESPACE pg_default;

