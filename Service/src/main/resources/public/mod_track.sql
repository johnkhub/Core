CREATE TABLE public.modtrack_meta (
    recid bigserial NOT NULL,
    "version" int4 NOT NULL,
    "identity" uuid NOT NULL,
    CONSTRAINT modtrack_meta_pkey PRIMARY KEY (recid)
);


CREATE TABLE public.modtrack_tables (
    recid bigserial NOT NULL,
    tablename varchar NOT NULL,
    createcount int8 NOT NULL,
    stamp int8 NOT NULL,
    CONSTRAINT modtrack_tables_pkey PRIMARY KEY (recid)
);
CREATE UNIQUE INDEX modtrack_tables_tablename_idx ON public.modtrack_tables USING btree (tablename);


CREATE TABLE public.migration_version (
    "version" int4 NULL
);