SELECT '====================== 14_SetAccess.sql =====================';

DELETE FROM access_control.entity_access;


SELECT 'Readers...';
--  grant group 'Readers', Read access to all entities
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 
    2, 
    ARRAY(SELECT asset_id FROM asset), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'Readers')
);

-- assign permissions to CD groups: we only give them READ access. To include update change to 6
SELECT 'CD_PPP...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_PPP' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_PPP')
);

SELECT 'CD_IAM...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_IAM' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_IAM')
);


SELECT 'CD_GI...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_GI' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_GI')
);

SELECT 'CD_EI...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_EI' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_EI')
);

SELECT 'CD_HI...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_HI' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_HI')
);

SELECT 'CD_RNM...';
SELECT access_control.sp_grant_access(
    (access_control.fn_get_system_user()), 2, 
    ARRAY(
        SELECT asset.asset_id FROM 
        asset JOIN asset_classification ON asset.asset_id = asset_classification.asset_id 
        WHERE 'CD_RNM' =  (SELECT chief_directorate_code FROM dtpw.ref_client_department WHERE k = responsible_dept_code)
    ), 
    (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_RNM')
);


-- For the Create permission we actually need to tie the permission to the type of asset rather than the asset instance. To achieve this, we have a uuid for each asset 
-- type and insert that into the  entity_access table.

SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_HI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_PPP'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_IAM'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_GI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_EI'));
SELECT access_control.sp_grant_access((access_control.fn_get_system_user()), 1,ARRAY((SELECT uid FROM assettype)), (SELECT group_id FROM access_control."group" WHERE NAME = 'CD_RNM'));

-- So ... to check if you have permissions to create an ENVELOPE:   SELECT(access_control.fn_get_effective_access(<user uuid>, (SELECT uid FROM assettype WHERE code = 'ENVELOPE')) & 4 must be = 4