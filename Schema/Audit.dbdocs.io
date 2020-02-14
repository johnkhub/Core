Table audit {
  audit_id uuid PK
  principal_id uuid [NOT NULL, note: 'Principal that posted Transaction']
  principal_name varchar [NOT NULL]
  insert_time timestamp [NOT NULL, DEFAULT: `CURRENT_TIMESTAMP`]
  event_time timestamp [NOT NULL]
  action varchar [NOT NULL, note: 'Action like create, delete, login etc.']
  status varchar [NOT NULL, note: 'Succeeded, failed etc.']
  
  //... ???
 
  // TEMPORARILY REMOVED tamper_check varchar [NOT NULL] 
  
  Indexes {
    audit_id
    (event_time, principal_id)
    (event_time, action, status)
  }
}

Table auditlink {
  asset_id uuid PK
  audit_id uuid PK [UNIQUE]
  
  Indexes {
    (asset_id,audit_id)
  }
}

Ref: auditlink.asset_id > audit.audit_id

