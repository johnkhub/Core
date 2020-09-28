-- See DTPW.GROUPING_TYPE_EMIS
INSERT INTO grouping_id_type (type_id, name, description) VALUES ('4a6a4f78-2dc4-4b29-aa9e-5033b834a564', 'EMIS', 'DTPW EMIS') ON CONFLICT (type_id) DO NOTHING;