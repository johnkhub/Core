
/* 

From mirror database 

SELECT 
    rowid, "Locality", "MetaID", "MapFeatureID", 
    "ComponentID", √
    "ClientAssetID", "AssetMoveableID", "AssetOwnerID", "WardNr", "SuburbName", "RegionName", 
    "LocationAddress", √
    "LocationStand",  "LocationSGcode", 
    "AccountingGroupID", "AssetCategoryID", "AssetSubCategoryID", "AssetGroupTypeID", "AssetTypeID", "ComponentTypeID", √
    "AssetFacilityName", √
    "DescriptorType", "DescriptorSize", "DescriptorClass", √
    "DescriptorSize_Unit", "Extent", "Extent_Unit", "Extent_Unit_Rate", "YearConstruct", "YearRenewed", 
    "EUL", "RUL", "CRC", 
    "CriticalityGrade", "ConditionGrade", "PerformanceGrade", "UtilisationGrade", √
    "TakeOnDate", "CostOpening", "CostClosing", "CarryingValueOpening", 
    "CarryingValueClosing", "ResidualValue", "DepreciationMethodID", "DepreciationOpening", "DepreciationFinYTD", "DepreciationClosing", "ImpairmentAll", "ImpairmentFinYTD", 
    "RevImpairmentFinYTD", "ImpairmentDerecog", "ImpairmentClose", "RiskExposure", "ForecastReplYear", "CRC_Adjusted", "FairValue", "AnnualisedMaintPct", "MaintenanceBudgetNeed", 
    "MaintenanceBudgetPct", "MaintenanceExpenditure", 
    "CriticalityGrade_CG", "ConditionGrade_CG", "PerformanceGrade_CG", "UtilisationGrade_CG", "OpsCostGrade", "OpsCostGrade_CG", √
    "RevaluedAmount", "ComponentDesc", "ResidualPct", "InfraAssetTypeID", "EquipmentNr", 
    "Latitude", "Longitude", √
    "TreasuryCode", "CostCentreCode", "GeneralLedgerCode", "WIP_Project_ID", 
    "MeasurementModel", "RespDepartmentID", "AssetCustodianID", "CustodianshipDate", "ImpairmentDate", "ImpairmentReason", "RevImpairmentDate", "RevImpairmentReason", "DisposalMethodID", 
    "TransferDate", "TransferredFrom", "TransferredTo", "DerecognitionDate", "Asset_Barcode_Nr", 
	"Room_Barcode_Nr", √
	"Serial_Number", "Fleet_Number", "AdditionsFinYTD", "ValueChangeFinYTD", 
    "ProvisionOpening", "ProvisionAdjust", "ProvisionClosing", "DisposalProceedCost", "DerecognitionCost", "DerecognitionDepr", "TransferCost", "TransferDepr", "ImpairmentTransfer", 
	"Room_Name", √
    "Building_Name", "Fleet_Reg_Year", 
	"Floor", √
	"AdditionsNature", "CaseNumber", "CashGenerating", "DateCreated", "DateLastFinMonth", "DateLastRenewed", "DepreciationBudgetNr_Credit", 
    "DepreciationBudgetNr_Debit", "FuncLocPath", "Impairment_Review_Date", "InsuranceClaimed", "InsurancePolicyNr", "LastMaintenanceDate", "ReplacedComponents", "RevaluationAccuracy", 
    "RevaluationLastDate", "RevaluationMethod", "RevaluationNextDate", "RevaluedBy", "SoldTo", "UpgradeDate", "VerificationLastDate", "VerificationNextDate", "YearConstruct_CG", "YearRenewed_CG", 
    "AdditionsClosing", "AdditionsOpening", "DisposalProfitLoss", "DRC", "EUL_Adjusted", "FairValueLessCostSell", "InsuranceAmount", "InsuranceCover", "RevaluationReserveClosing", 
    "RevaluationReserveFinYTD", "RevaluationReserveFinYTDDepr", "RevaluationReserveFinYTDImp", "RevaluationReserveOpening", "RevaluationRUL", "RevImpairmentAll", "RevImpairmentClose", 
"   ValueInUse", "PreviousComponentID"
FROM public."AssetComponent"; 
*/

CREATE OR REPLACE VIEW ar_lite_view AS
SELECT
	asset.asset_id,
/*	
    asset_link.external_id AS "ComponentID" ,
 
    -- split financial path here
    split_part(asset.grap_path, '-', 1) AS "AccountingGroupID",
	split_part(asset.grap_path, '-', 2) AS "AssetCategoryID",
	split_part(asset.grap_path, '-', 3) AS "AssetSubCategoryID",
	split_part(asset.grap_path, '-', 4) AS "AssetGroupTypeID",
	split_part(asset.grap_path, '-', 5) AS "AssetTypeID",
	split_part(asset.grap_path, '-', 6) AS "ComponentTypeID",
*/	

/*	
	-- split adm path here THIS IS WRONG WE WOULD NEED TO DO A LOOKUP FROM THE CLASSIFICATION DATABASE
	split_part(asset.adm_path, '-', 3) AS "DescriptorType",
	split_part(asset.adm_path, '-', 4) AS "DescriptorClass",
	split_part(asset.adm_path, '-', 5) AS "DescriptorSize",
*/
	location.latitude AS "Latitude",
    location.longitude AS "Longitude",
    --location.address AS "LocationAddress",

    -- facility
	"facility".name AS "AssetFacilityName", 
/*	
	-- lifecycle
    lifecycle.criticality_grade AS "CriticalityGrade", 
    lifecycle.condition_grade AS "ConditionGrade", 
    lifecycle.performance_grade AS "PerformanceGrade", 
    lifecycle.utilisation_grade AS "UtilisationGrade", 
    lifecycle.ops_cost_grade AS "OpsCostGrade",

    lifecycle.criticality_grade_cg AS "CriticalityGrade_CG", 
    lifecycle.condition_grade_cg AS "ConditionGrade_CG", 
    lifecycle.performance_grade_cg AS "PerformanceGrade_CG", 
    lifecycle.utilisation_grade_cg AS "UtilisationGrade_CG", 
    lifecycle.ops_cost_grade_cg AS "OpsCostGrade_CG",
*/
	-- floor
	"floor".name AS "floor",
	
	-- room
	room.name AS "Room_Name"
	--room.barcode AS "Room_Barcode_Nr"

    -- financials
	
FROM
    asset LEFT JOIN asset_link on asset.asset_id = asset_link.asset_id and asset_link.external_id_type = 'c6a74a62-54f5-4f93-adf3-abebab3d3467'
	
	LEFT JOIN far.financials ON asset.asset_id = far.financials.asset_id
    LEFT JOIN far.lifecycle ON asset.asset_id = far.lifecycle.asset_id
    LEFT JOIN location  ON asset.asset_id = location.asset_id
	
	LEFT JOIN asset "facility" ON "facility".func_loc_path <@ asset.func_loc_path AND "facility".asset_type_code = 'FACILITY'
    LEFT JOIN asset "floor" ON "floor".func_loc_path <@ asset.func_loc_path AND "floor".asset_type_code = 'FLOOR'
    LEFT JOIN asset "room" ON "room".func_loc_path <@ asset.func_loc_path AND "room".asset_type_code = 'ROOM';

/*
CREATE EXTENSION tablefunc;

select * FROM crosstab(
	'
	SELECT f."templateId", f."fieldName", f.default
	FROM "TemplateType" t
	JOIN "FieldTemplateDefinition" f ON t.id = f."templateId" AND t."isActive"
	',
	'
	SELECT DISTINCT("fieldName") FROM "FieldTemplateDefinition" ORDER BY "fieldName"
	'
) AS T (
	id bigint,
	"descriptorSizeUnit" text,
	"eul" numeric,
	"eulUnit" text,
	"extentConversion" numeric,
	"extentUnit" text,
	"refUnitRateUnit" text, 
	"residualPct" numeric,
	"unitRate" numeric, 
	"unitRateCG" int 
);

*/