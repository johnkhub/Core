CREATE SCHEMA "far";

CREATE TABLE "far"."lifecycle" (
  "asset_id" uuid PRIMARY KEY,
  "criticality_grade" int8 CHECK(criticality_grade >= 1 AND criticality_grade <= 5),
  "criticality_grade_cg" int8 CHECK(criticality_grade_cg >= 1 AND criticality_grade_cg <= 5),
  "condition_grade" int8 CHECK(criticality_grade >= 1 AND criticality_grade <= 5),
  "condition_grade_cg" int8 CHECK(condition_grade_cg >= 1 AND condition_grade_cg <= 5),
  "performance_grade" int8 CHECK(performance_grade >= 1 AND performance_grade <= 5),
  "performance_grade_cg" int8 CHECK(performance_grade_cg >= 1 AND performance_grade_cg <= 5),
  "utilisation_grade" int8 CHECK(utilisation_grade >= 1 AND utilisation_grade <= 5),
  "utilisation_grade_cg" int8 CHECK(utilisation_grade_cg >= 1 AND utilisation_grade_cg <= 5),
  "ops_cost_grade" int8 CHECK(criticality_grade >= 1 AND criticality_grade <= 5),
  "ops_cost_grade_cg" int8 CHECK(ops_cost_grade_cg >= 1 AND ops_cost_grade_cg <= 5),
  "ops_utilisation_grade" int8 CHECK(ops_utilisation_grade >= 1 AND ops_utilisation_grade <= 5),
  "ops_utilisation_grade_cg" int8 CHECK(ops_utilisation_grade_cg >= 1 AND ops_utilisation_grade_cg <= 5),

  "eul" int CHECK(eul >= 0),
  "eul_adjusted" int,
  "rul" int  CHECK(eul >= 0)
);

CREATE TABLE "far"."financials" (
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
