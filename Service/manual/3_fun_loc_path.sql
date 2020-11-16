DROP INDEX public.func_loc_path_idx;

CREATE UNIQUE INDEX func_loc_path_idx
    ON public.asset USING btree
        (func_loc_path ASC NULLS LAST)
    TABLESPACE pg_default;