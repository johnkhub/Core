Table transaction {
  transaction_id uuid PK
  transaction_type_code varchar(10) [NOT NULL]
  asset_id uuid  [NOT NULL]
  batch_id uuid [NOT NULL]
  
  insert_date timestamp [NOT NULL, default: `CURRENT_TIMESTAMP`]
  submit_date timestamp [NOT NULL]
  effective_date timestamp [NOT NULL]
  reason varchar [NULL]
  field varchar  [NOT NULL]
  
  // Useful to maintain this type of variant representation to do aggregation in db
  delta_T_SLONG bigint
  delta_T_ULONG bigint [note:'Add checked constraint > 0 OR NULL']
  amount decimal(19,6)
  delta_T_STRING varchar
  delta_T_BOOLEAN boolean
  delta_T_POLYGON varchar
  // Add check constraint that one and only one of them must be non-NULL
  
  // REMOVED TEMPORARILY audit_id uuid [NOT NULL, note: 'audit log entry']
 
  // REMOVED TEMPORARILY tamper_check varchar [NOT NULL]
  
  // REMOVED TEMPORARILY  meta jsonb [default: `'{}'::jsonb`, note: 'arbitrary annotations']
  
  // We will probably have a Correlation table that links messages/transactions
  correlation_id uuid [NULL, note: 'Correlate to messages within the system']

  // We will probably have to invest a lot of time in properly
  // indexing this. Will require some analysis
  Indexes {
    (transaction_id, asset_id)
    batch_id
    correlation_id
    (effective_date,asset_id)
  }
}

Table transaction_type {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  description text
}

Table transaction_batch {
  batch_id uuid PK  [UNIQUE]
  comments text 
  // REMOVED TEMPORARILY  meta jsonb [default: `'{}'::jsonb`, note: 'We can store the actual form in here?']
}


Ref: transaction_batch.batch_id < transaction.batch_id
Ref: transaction.transaction_type_code > transaction_type.code

Enum data_type {
  T_SLONG
  T_ULONG
  T_MONEY
  T_STRING
  T_BOOLEAN
  T_POLYGON
  T_DATETIME
}



Table field {
    name varchar PK  [UNIQUE]
    fqn varchar [NULL, UNIQUE, note: '<schema>.<table>.<column> used to map values into snapshot tables']
    //TEMP MEASURE INTEROP WITH TSQL type data_type [NOT NULL]
    type varchar(10)
    // REMOVED TEMPORARILY field_attributes jsonb [NOT NULL, default: `'{}'::jsonb`]
}

Ref: transaction.field >  field.name