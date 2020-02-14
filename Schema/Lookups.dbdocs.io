// Use PG table extension concept
Table kv_base {
  k varchar(10) PK
  v varchar
  
  creation_date timestamp [NOT NULL, DEFAULT: 'NOW()']
  audit_id uuid [NOT NULL]
  deactivated_at timestamp [NULL]
  allow_delete boolean [DEFAULT:'false']
 
  // TEMPORARILY REMOVED tamper_check varchar [NOT NULL]
  
  Indexes {
    k
  }
}

Table kv_base_bidirectional {
  k varchar(10) PK
  v varchar [note: 'Inerits from KvBase']
  
  Indexes {
    v [UNIQUE]
  }
}

Table kv_type {
  code varchar(10) PK
  name varchar
  description varchar
  owner uuid [UNIQUE]
  
  Indexes {
    code
    owner
  }
}
