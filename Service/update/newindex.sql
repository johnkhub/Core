-- Constraint: asset_identification_pkey

-- ALTER TABLE public.asset_identification DROP CONSTRAINT asset_identification_pkey;

ALTER TABLE public.asset_identification
    ADD CONSTRAINT asset_identification_pkey PRIMARY KEY (asset_id);
