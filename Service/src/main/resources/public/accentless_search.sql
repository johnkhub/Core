CREATE EXTENSION IF NOT EXISTS "unaccent";

CREATE OR REPLACE FUNCTION public.f_unaccent(text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE PARALLEL SAFE STRICT
AS $function$
SELECT public.unaccent('public.unaccent', $1)  -- schema-qualify function and dictionary
$function$
;


//
CREATE INDEX ON asset (lower(f_unaccent(name)));
CREATE INDEX ON location (lower(f_unaccent(address)));
//
