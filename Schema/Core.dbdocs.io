//
//
// The Core represents the data that is common to all assets
//
//

Table asset {
  asset_id uuid PK
  asset_type_code varchar(10) [NOT NULL]
  
  adm_path varchar [note: 'Asset may or may not have one. Links to Template Service']
  func_loc_path varchar
  grap_path varchar 
  // Add paths as required
  
  // These two fields will also be in the audit log, but I think they will be queried
  // often
  creation_date timestamp [NOT NULL, default: 'NOW()']
  deactivated_at timestamp [NULL, note: 'WE DO NOT DELETE']
  
  reference_count BIGINT [NOT NULL, default: 0, note: 'We should be allowed to delete terminal nodes that have no transactions against them?']
  
  // REMOVED TEMPORARILY tamper_check varchar [NOT NULL] 
    
  Indexes {
    asset_id
    asset_type_code
    
    adm_path
    func_loc_path
    grap_path
    // add index for each path ...
    
    deactivated_at
  }
}

Table assettype {
  code varchar(10) PK
  name varchar [NOT NULL]
  description varchar
  
  // We may need additional fields here
  // Make sure that we understand what must be part of the ADM and what not!
  
  Indexes {
    code
  }  
}

Ref: asset.asset_type_code > assettype.code

// 
// Spatial representation
//
Table mapfeature {
  // Same asset could have multiple different polygons
  asset_id uuid PK
  mapfeature_type_code varchar(10)
  polygon varchar [NULL]
  
  // REMOVED TEMPORARILY tamper_check varchar [NOT NULL]
}
Ref: asset.asset_id < mapfeature.asset_id


Table mapfeature_type {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  // ...
  
  Indexes {
    code
  }
}

Ref: mapfeature.mapfeature_type_code > mapfeature_type.code


//
// Location
//
Table location {
  asset_id uuid PK [UNIQUE]
  location_path varchar [NOT NULL]
  latitude decimal(9,6) [NOT NULL]
  longitude decimal(9,6) [NOT NULL]
  address varchar(80) [NULL]
  
  //REMOVED TEMPORARILY tamper_check varchar [NOT NULL]
    
  Indexes {
    location_path
  }
}
Ref: location.asset_id - asset.asset_id 


//
// Linking table to link Asset to identifiers in 3rd party
// systems.  Each external system has a unque identifier
//
Table asset_link  {
  asset_id uuid PK
  external_id varchar PK
  external_id_type uuid [NOT NULL]
  
  Indexes {
    (external_id_type,asset_id,external_id)
    (external_id_type,external_id,external_id_type)
  }
}

Table external_id_type {
  type_id uuid PK [note: 'E.g. Asset ID in SAP']
  name varchar  [NOT NULL]
  description varchar  [NOT NULL]
}

Ref: asset_link.external_id_type > external_id_type.type_id
Ref: asset.asset_id < asset_link.asset_id


//Ref: asset.asset_id - lifecycle.asset_id
//Ref: asset.asset_id - financials.asset_id
//Ref: asset.asset_id < transaction.asset_id
//Ref: asset.asset_id - assetfacility.asset_id