//
//
// Data that is used by many modules but that does not 
// form part of the intrinsic properties of an asset
//
//

//
// Suppliers
//
Table supplier { 
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL, UNIQUE]
  description varchar
  supplier_type_code  varchar(10) [NOT NULL]
  
  Indexes {
    code
  }
}

Table supplier_type {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  description varchar  
}
Ref: supplier.supplier_type_code > supplier_type.code

//
// Unit
//
Table unit {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  is_si boolean [NOT NULL, note:'Is SI or Imperial?'] 
  symbol varchar [NOT NULL]
  type unit_type
}
// It makes sense to add meta-data to the database so we can
// display a subset of values based on queries

Enum unit_type {
  T_TIME
  T_LENGTH
  T_MASS
  T_CURRENT
  T_TEMPERATURE
  T_LUMINOSITY
  T_VOLTAGE
  T_POWER
  T_VOLUME
  T_AREA
  T_CURRENCY
  T_VELOCITY
  T_DENSITY
  T_PRESSURE
}

//
// Facility
//
Table facility {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  description varchar
  facility_type_code varchar(10) [NOT NULL]
  
  Indexes {
    code 
  }
}

Table facility_type {
  code varchar(10) PK [UNIQUE]
  name varchar [NOT NULL]
  description varchar
  Indexes {
    code 
  }
}

Table asset_facility {
  asset_id uuid PK 
  facility_code varchar(10) PK
  
  Indexes {
    (asset_id,facility_code) [UNIQUE]
  }
}

Ref: facility.facility_type_code > facility_type.code
Ref: asset_facility.facility_code > facility.code