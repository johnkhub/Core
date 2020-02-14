INSERT INTO assettype (code, name, uid) VALUES ('ENVELOPE', 'Envelope', uuid_generate_v4());
INSERT INTO assettype (code, name, uid) VALUES ('LANDPARCEL', 'Land Parcel', uuid_generate_v4());
INSERT INTO assettype (code, name, uid) VALUES ('FACILITY', 'Facility', uuid_generate_v4());
INSERT INTO assettype (code, name, uid, description) VALUES ('SITE', 'Site', uuid_generate_v4(), 'Site resides under Facility and is used to group things like fences that are not part of a Building');
INSERT INTO assettype (code, name, uid, description) VALUES ('BUILDING', 'Building',  uuid_generate_v4(), 'Building in a Facility');
INSERT INTO assettype (code, name, uid, description) VALUES ('FLOOR', 'Floor', uuid_generate_v4(), 'Floor in a Building');
INSERT INTO assettype (code, name, uid, description) VALUES ('ROOM', 'Room', uuid_generate_v4(), 'Room on a Floor');
INSERT INTO assettype (code, name, uid, description) VALUES ('COMPONENT', 'Component', uuid_generate_v4(), 'This is a placeholder - we will break these down further later');