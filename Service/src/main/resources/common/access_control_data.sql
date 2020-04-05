-- Standard permission flags
INSERT INTO access_control.access_type (mask, name) VALUES (0, 'NONE');
INSERT INTO access_control.access_type (mask, name) VALUES (1, 'CREATE');
INSERT INTO access_control.access_type (mask, name) VALUES (2, 'READ');
INSERT INTO access_control.access_type (mask, name) VALUES (4, 'UPDATE');
INSERT INTO access_control.access_type (mask, name) VALUES (8, 'DELETE');

-- Add a system user
INSERT INTO access_control.principal (principal_id, group_id, name, description, is_group, reserved)
VALUES (uuid_generate_v4(), null, 'System', 'System user is the root grantor of permissions.', false, true);