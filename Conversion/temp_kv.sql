DROP TABLE IF EXIST public.temp_kv

CREATE TABLE public.temp_kv (
	k varchar(10) NOT NULL,
	v varchar NULL,
	creation_date timestamp NOT NULL DEFAULT now(),
	deactivated_at timestamp NULL,
	allow_delete bool NULL DEFAULT false,
	activated_at timestamp NOT NULL DEFAULT now(),
	CONSTRAINT temp_kv_k_check CHECK ((((k)::text <> ''::text) AND ((k)::text ~ '^[\w]*$'::text)))
);
CREATE UNIQUE INDEX temp_kv_k_idx ON public.temp_kv USING btree (k);