CREATE OR REPLACE FUNCTION public.fn_add_tags(a uuid, t text[]) RETURNS void AS $$
DECLARE
    invalid text[];
BEGIN
    -- identify the values in t that are not in the table of defined tags
    invalid := 	ARRAY(SELECT u FROM unnest(ARRAY[t]) u LEFT JOIN tags ON u = tags.k WHERE tags.k IS NULL);
    IF (array_length(invalid,1) > 0) THEN
       RAISE EXCEPTION 'Tag(s) % are not defined', invalid;
    END IF;

    INSERT INTO asset_tags (asset_id,tags) VALUES (a,t)
    ON CONFLICT (asset_id)
    DO
    UPDATE SET tags = EXCLUDED.tags ||   -- append the values not already present
		ARRAY(
			-- This expands the arrays into rows and joins them to determine which ones are new
			SELECT to_add FROM
				unnest(t) to_add
			LEFT JOIN
				unnest(asset_tags.tags) existing
			ON existing = to_add WHERE existing IS NULL
		),
	asset_id = EXCLUDED.asset_id;
END; $$
LANGUAGE PLPGSQL
SECURITY DEFINER
;

COMMENT ON FUNCTION public.fn_add_tags IS 'Adds the specified tags to the asset with the specified UUID';

CREATE OR REPLACE FUNCTION public.fn_has_tags(a uuid, t text[]) RETURNS boolean AS $$
BEGIN
    RETURN coalesce((SELECT tags FROM asset_tags WHERE asset_id = a) @> t, false); --@> means contains
END; $$
LANGUAGE PLPGSQL
SECURITY DEFINER
;

CREATE OR REPLACE FUNCTION public.fn_remove_tags(a uuid, t text[]) RETURNS void AS $$
DECLARE
    invalid text[];
BEGIN
    -- identify the values in t that are not in the table of defined tags
    invalid := 	ARRAY(SELECT u FROM unnest(ARRAY[t]) u LEFT JOIN tags ON u = tags.k WHERE tags.k IS NULL);
    IF (array_length(invalid,1) > 0) THEN
       RAISE EXCEPTION 'Tag(s) % are not defined', invalid;
    END IF;

    FOR i IN 1..array_upper(t,1)
    LOOP
        UPDATE asset_tags SET tags = array_remove(tags, t[i]) WHERE asset_id = a;
    END LOOP;

    -- there is a fk constraint between asset and asset_tag, so we need to remove the dead entry to be able
    -- to remove the asset
    DELETE FROM asset_tags WHERE asset_id = a AND cardinality(tags) = 0; -- "array_length(tags,1) = 0;" doe snot work!
END; $$
LANGUAGE PLPGSQL
SECURITY DEFINER
;
