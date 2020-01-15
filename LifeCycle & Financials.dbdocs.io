Table lifecycle {
  asset_id uuid  PK

  //
  // I reject the idea of a database default for these fields. They should either
  // be intentionally populated or NULL
  //
  criticality_grade int8 [NULL]
  criticality_grade_cg int8 [NULL]
  
  condition_grade int8 [NULL]
  condition_grade_cg int8 [NULL]
  
  performance_grade int8 [NULL]
  performance_grade_cg int8 [NULL]
  
  utilisation_grade int8 [NULL]
  utilisation_grade_cg int8 [NULL]
  
  ops_cost_grade int8 [NULL]
  ops_cost_grade_cg int8 [NULL]
  
  ops_utilisation_grade int8 [NULL]
  ops_utilisation_grade_cg int8 [NULL]
  
  eul int [NULL]
  eul_adjusted int [NULL]
  rul int [NULL]
}


// Need to get precise definitions of all of these fields so we can figure out how to calculate
//  one from the other so we store minimal number of fields

Table financials {
  asset_id uuid  PK
    
  depreciation decimal(19,4)
  impairment decimal(19,4)
  carrying_value decimal(19,4)
  cost decimal(19,4)
  transfer_cost decimal(19,4)
  
//--  
  // reclas...
  
  // reval

  disposal_proceed_cost decimal(19,4)
  disposal_profit_loss decimal(19,4)
  derecognition_cost decimal(19,4)
  derecognition_depr decimal(19,4)
  impairment_derecog decimal(19,4)
  revaluation_reserve_opening decimal(19,4)
  transfer_depr decimal(19,4)
  impairment_transfer decimal(19,4)
  
  
  revaluation_rul decimal(19,4)
  maintenance_expenditure decimal(19,4)
  crc decimal(19,4)

//--
  additions decimal(19,4)
}
