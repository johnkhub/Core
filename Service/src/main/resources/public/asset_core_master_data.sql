INSERT INTO assettype (code, name, uid) VALUES ('ENVELOPE', 'Envelope', '20430440-b8d4-45db-bedb-dbbfbe6699c6') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid) VALUES ('LANDPARCEL', 'Land Parcel', '3c2faf23-9011-4458-9d8b-d4ffadbaeb9a') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid) VALUES ('FACILITY', 'Facility', '10fdd030-7d94-46ce-b3c9-ae82d1c0f4bd') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid, description) VALUES ('SITE', 'Site','9d92001f-bed0-484f-b2ae-d5fee4f4993d' , 'Site resides under Facility and is used to group things like fences that are not part of a Building') ON CONFLICT (code)DO NOTHING;
INSERT INTO assettype (code, name, uid, description) VALUES ('BUILDING', 'Building',  'e2963409-44eb-480f-b12f-bde4ea4f3c52', 'Building in a Facility') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid, description) VALUES ('FLOOR', 'Floor', '03298160-03d6-4d14-bd7e-1dde7e771871', 'Floor in a Building') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid, description) VALUES ('ROOM', 'Room', 'd6adf53d-0ad9-4047-ae72-334f5a15853d', 'Room on a Floor') ON CONFLICT (code) DO NOTHING;
INSERT INTO assettype (code, name, uid, description) VALUES ('COMPONENT', 'Component', '37144c0d-615f-4096-807c-d80c51c6a762', 'This is a placeholder - we will break these down further later') ON CONFLICT (code) DO NOTHING;

INSERT INTO external_id_type (type_id, name, description) VALUES ('c6a74a62-54f5-4f93-adf3-abebab3d3467', 'V6', 'Version 6 component identifier') ON CONFLICT (type_id) DO NOTHING;
