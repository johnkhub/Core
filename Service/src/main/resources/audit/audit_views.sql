CREATE OR REPLACE VIEW audit.audit_view AS
SELECT
    a.audit_id,
    p.name,
    a.event_time,
    a.action,
    a.status,
    a.parameters,
    l.entity_id
FROM audit.audit a
JOIN access_control.principal p ON a.principal_id = p.principal_id
LEFT JOIN audit.auditlink l ON a.audit_id = l.audit_id
;
//