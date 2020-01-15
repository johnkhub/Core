CREATE TABLE "lifecycle" (
  "asset_id" uuid PRIMARY KEY,
  "criticality_grade" int8,
  "criticality_grade_cg" int8,
  "condition_grade" int8,
  "condition_grade_cg" int8,
  "performance_grade" int8,
  "performance_grade_cg" int8,
  "utilisation_grade" int8,
  "utilisation_grade_cg" int8,
  "ops_cost_grade" int8,
  "ops_cost_grade_cg" int8,
  "ops_utilisation_grade" int8,
  "ops_utilisation_grade_cg" int8,
  "eul" int,
  "eul_adjusted" int,
  "rul" int
);

CREATE TABLE "financials" (
  "asset_id" uuid PRIMARY KEY,
  "depreciation" decimal(19,4),
  "impairment" decimal(19,4),
  "carrying_value" decimal(19,4),
  "cost" decimal(19,4),
  "transfer_cost" decimal(19,4),
  "disposal_proceed_cost" decimal(19,4),
  "disposal_profit_loss" decimal(19,4),
  "derecognition_cost" decimal(19,4),
  "derecognition_depr" decimal(19,4),
  "impairment_derecog" decimal(19,4),
  "revaluation_reserve_opening" decimal(19,4),
  "transfer_depr" decimal(19,4),
  "impairment_transfer" decimal(19,4),
  "revaluation_rul" decimal(19,4),
  "maintenance_expenditure" decimal(19,4),
  "crc" decimal(19,4),
  "additions" decimal(19,4)
);
