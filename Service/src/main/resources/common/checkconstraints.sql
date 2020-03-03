ALTER TABLE public.asset_identification ADD CONSTRAINT asset_identification_barcode_check CHECK (barcode::text <> ''::text);
ALTER TABLE public.asset_identification ADD CONSTRAINT asset_identification_serial_number_check CHECK (serial_number::text <> ''::text);
ALTER TABLE public.kv_base ADD CONSTRAINT kv_base_k_check CHECK (k::text <> ''::text AND k::text ~ '^[\w]*$'::text);
ALTER TABLE public.kv_base ADD CONSTRAINT kv_base_k_check1 CHECK (k::text <> ''::text);
ALTER TABLE public.unit ADD CONSTRAINT unit_code_check CHECK (code::text ~ '^[\w]*$'::text);
ALTER TABLE public.unit ADD CONSTRAINT unit_name_check CHECK (name::text <> ''::text);
ALTER TABLE public.unit  ADD CONSTRAINT unit_symbol_check CHECK (symbol::text <> ''::text);
