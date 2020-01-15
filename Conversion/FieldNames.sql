/*

--
--  Generate Field insert scripts by interrogating the AssetRegisterIconFinXXXX table for the column names and data type
--  It will ignore computed fields as well a a list of hardcoded fields. The output is sorted alphabetically so modification 
--  is easier if individual insert lines need to be added removed.
--
--  Note that the type mapping may not be entirely correct
--
select 
	'INSERT INTO TABLE field (name, type, fqn) VALUES ('''+c.name +''',''' +
	case 
		when tp.name = 'int' then 'T_SLONG'
		when tp.name = 'numeric' then 'T_MONEY'
		when tp.name = 'varchar' then 'T_STRING'
		when tp.name = 'datetime' then 'T_DATETIME'
		when tp.name = 'bit' then 'T_BOOLEAN'
	end
	+''',''UNDEFINED'');'

from  sys.columns c 
	inner join sys.tables t on c.object_id = t.object_id 
	inner join sys.types tp ON tp.system_type_id = c.system_type_id
where 
	t.name = 'AssetRegisterIconFin2019' and is_computed = 0 
	and c.name not in ( 
		-- intermediate flag for v6 to v8 sync
		'DirtyFlag',
		
		'FAR_ID', -- don't think we need this

		-- ADM path
		'DescriptorType',
		'DescriptorSize',
		'DescriptorClass',
		-- ADM VALUES
		'Extent_Unit',
		'Extent_Unit_Rate',
		'extentConversion',
		'EUL',
		'ResidualPct',
		'DescriptorSize_Unit',

		-- Asset Hierarchy will become a path
		'AccountingGroupID',
		'AssetCategoryID',
		'AssetSubCategoryID',
		'AssetGroupID',
		'AssetTypeID',
		'ComponentType',

		-- SCOA can ignore for now
		'SCOA_Item_Depreciation_Debit',
		'SCOA_Item_Depreciation_Credit',
		'DepreciationBudgetNr_Debit',
		'DepreciationBudgetNr_Credit',
		'SCOAAssignmentID',

		
			
		-- all null may differ from client to client
		'NetworkNr',
		'MapFeatureType',
		'MethodAcquiredID',
		'CustodianshipDate',
		'BasicMunService',
		'ApplicableContracts',
		'InsurancePolicyNr',
		'DebtSecurityExpiryDate',
		'YearConstruct_CG',
		'YearRenewed',
		'YearRenewed_CG',
		'AdditionsFundSourceID',
		'TransferDate',
		'RevaluationLastDate',
		'RevaluationNextDate',
		'RevaluationRUL',
		'RevaluationAccuracy',
		'ForecastReplValue',
		'AnnualisedMaintPct',
		'MaintenanceBudgetPct',
		'Impairment_Review_Date',
		'RefSuburbsCode',
		'CaseNumber',
		'InsuranceClaimed',
		'InsuranceAmount',
		'TransferredFrom',
		'TransferredTo',
		'FinHierarchyPath',
		'VerificationLastDate',
		'VerificationNextDate',
		'UpgradeDate',
		'LastMaintenanceDate',
		'ReplacedComponents',
		'SoldTo',
		'PreviousComponentID'
)  order by c.name asc
/*
Wait for TUSK db, it will have a fully populated ADM
They currently have a properly built Fnctional location path
*/


 OUTDATED ! run the script again !!!!
  
INSERT INTO TABLE field (name, type) VALUES ('AdditionsFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('AdditionsFundTypeID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('AdditionsNature','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('AdditionsOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('AssetCustodianID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('AssetFacilityID','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('AssetFacilityName','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('AssetMoveableID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('AssetOwnerID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('BOQPath','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('CarryingValueOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('CashGenerating','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ClientAssetID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ComponentDesc','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ComponentID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ConditionGrade','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('ConditionGrade_CG','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('CostCentreCode','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('CostOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('CRC','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('CRC_Adjusted','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('CriticalityGrade','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('CriticalityGrade_CG','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('DateCreated','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('DateLastFinMonth','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('DateLastRenewed','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('DebtSecurityApplicable','T_BOOLEAN');
INSERT INTO TABLE field (name, type) VALUES ('DepreciationFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('DepreciationMethodID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('DepreciationOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('DerecognitionCost','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('DerecognitionDate','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('DerecognitionDepr','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('DisposalMethodID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('DisposalProceedCost','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('DisposalProfitLoss','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('EUL_Adjusted','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('Extent','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('FairValue','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('FairValueLessCostSell','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('FAR_ID','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('FuncLocPath','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('FundingSourceID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('FundingTypeID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('GeneralLedgerCode','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentAll','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentDate','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentDerecog','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentReason','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('ImpairmentTransfer','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('InsuranceCover','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('Latitude','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('LocationAddress','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('LocationSGcode','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('LocationStand','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('Longitude','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('MaintenanceExpenditure','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('MapFeatureID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('MeasurementModel','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('MeasurementModelID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('OpsCostGrade','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('OpsCostGrade_CG','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('OwnedLeasedID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('PerformanceGrade','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('PerformanceGrade_CG','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('ProvisionAdjust','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('ProvisionOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RegionName','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('RespDepartmentID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('RevaluationMethod','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('RevaluationReserveFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevaluationReserveFinYTDDepr','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevaluationReserveFinYTDImp','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevaluationReserveOpening','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevaluedAmount','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevaluedBy','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('RevImpairmentAll','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RevImpairmentFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('RiskExposure','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('RUL','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('SuburbName','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('SupplierID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('TakeOnDate','T_DATETIME');
INSERT INTO TABLE field (name, type) VALUES ('TransferCost','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('TransferDepr','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('TreasuryCode','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('UseStatusID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('UtilisationGrade','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('UtilisationGrade_CG','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('ValueChangeFinYTD','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('ValueInUse','T_MONEY');
INSERT INTO TABLE field (name, type) VALUES ('WardNr','T_SLONG');
INSERT INTO TABLE field (name, type) VALUES ('WIP_Project_ID','T_STRING');
INSERT INTO TABLE field (name, type) VALUES ('YearConstruct','T_SLONG');