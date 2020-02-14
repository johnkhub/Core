--
--  This is a rework of the list of Fields as captured in FieldNames.sql
--  the idea is to map the fields such that we collapse open, closing and ytd VALUES into change of the same transactions.field
--  with the various v6 dates mapping to effective date
--  
--  In the initial phases of this experiment we will retain the V6 transactions.field names as much as possible to make it possible for most
--  people to modify this file
--

DELETE FROM transactions.field;

-- These fields are not present in V6. They are tracked in the Transaction tables, but live in the
-- Core hierarchical tables
INSERT INTO  transactions.field (name,type) VALUES ('grap_path','T_STRING');  
INSERT INTO  transactions.field (name,type) VALUES ('adm_path','T_STRING');  
INSERT INTO  transactions.field (name,type) VALUES ('func_loc_path','T_STRING');  



INSERT INTO  transactions.field (name,type) VALUES ('Additions','T_MONEY');  -- result of merge --> Additions
--INSERT INTO  transactions.field (name,type) VALUES ('AdditionsFinYTD','T_MONEY'); merged --> Additions
INSERT INTO  transactions.field (name,type) VALUES ('AdditionsFundTypeID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('AdditionsNature','T_STRING');
--INSERT INTO  transactions.field (name,type) VALUES ('AdditionsOpening','T_MONEY'); merged --> Additions
INSERT INTO  transactions.field (name,type) VALUES ('AssetCustodianID','T_STRING');

INSERT INTO  transactions.field (name,type) VALUES ('AssetFacilityId','T_SLONG'); 
--INSERT INTO  transactions.field (name,type) VALUES ('AssetFacilityName','T_STRING'); -- DON'T MAP THIS - remove from generating script

INSERT INTO  transactions.field (name,type) VALUES ('AssetMoveableID','T_STRING'); 
INSERT INTO  transactions.field (name,type) VALUES ('AssetOwnerID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('BOQPath','T_STRING');       
--INSERT INTO  transactions.field (name,type) VALUES ('CarryingValueOpening','T_MONEY'); calculated
INSERT INTO  transactions.field (name,type) VALUES ('CashGenerating','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('ClientAssetID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('ComponentDesc','T_STRING');

INSERT INTO  transactions.field (name,type) VALUES ('ComponentID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('ConditionGrade','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('ConditionGrade_CG','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('CostCentreCode','T_STRING');
--INSERT INTO  transactions.field (name,type) VALUES ('CostOpening','T_MONEY');  -- renamed --> Cost
INSERT INTO  transactions.field (name,type) VALUES ('Cost','T_MONEY'); -- result of rename --> Cost
INSERT INTO  transactions.field (name,type) VALUES ('CRC','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('CRC_Adjusted','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('CriticalityGrade','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('CriticalityGrade_CG','T_SLONG');
--INSERT INTO  transactions.field (name,type) VALUES ('DateCreated','T_DATETIME'); merged --> 
INSERT INTO  transactions.field (name,type) VALUES ('DateLastFinMonth','T_DATETIME');
INSERT INTO  transactions.field (name,type) VALUES ('DateLastRenewed','T_DATETIME');
INSERT INTO  transactions.field (name,type) VALUES ('DebtSecurityApplicable','T_BOOLEAN');
--INSERT INTO  transactions.field (name,type) VALUES ('DepreciationFinYTD','T_MONEY'); merged --> Depreciation
INSERT INTO  transactions.field (name,type) VALUES ('DepreciationMethodID','T_STRING');
--INSERT INTO  transactions.field (name,type) VALUES ('DepreciationOpening','T_MONEY'); merged --> Depreciation
INSERT INTO  transactions.field (name,type) VALUES ('Depreciation','T_MONEY'); -- result of merge --> Depreciation

-- INSERT INTO  transactions.field (name,type) VALUES ('DerecognitionCost','T_MONEY'); -- merged into Disposal
-- INSERT INTO  transactions.field (name,type) VALUES ('DerecognitionDate','T_DATETIME');  -- merged into Disposal
-- INSERT INTO  transactions.field (name,type) VALUES ('DerecognitionDepr','T_MONEY');  -- merged into Disposal
INSERT INTO  transactions.field (name,type) VALUES ('DisposalMethodID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('DisposalProceedCost','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('DisposalProfitLoss','T_MONEY');

INSERT INTO  transactions.field (name,type) VALUES ('EUL_Adjusted','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('Extent','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('FairValue','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('FairValueLessCostSell','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('FuncLocPath','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('FundingSourceID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('FundingTypeID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('GeneralLedgerCode','T_STRING');
-- INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentAll','T_MONEY');  merged --> Impairment
--INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentDate','T_DATETIME'); merged --> Impairment
--INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentDerecog','T_MONEY'); merged Disposal
--INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentFinYTD','T_MONEY'); merged -> Impairment
INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentReason','T_STRING'); --merged --> Impairment
--INSERT INTO  transactions.field (name,type) VALUES ('ImpairmentTransfer','T_MONEY'); --merged --> Impairment
INSERT INTO  transactions.field (name,type) VALUES ('InsuranceCover','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('Latitude','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('LocationAddress','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('LocationSGcode','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('LocationStand','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('Longitude','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('MaintenanceExpenditure','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('MapFeatureID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('MeasurementModel','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('MeasurementModelID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('OpsCostGrade','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('OpsCostGrade_CG','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('OwnedLeasedID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('PerformanceGrade','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('PerformanceGrade_CG','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('ProvisionAdjust','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('ProvisionOpening','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RegionName','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('RespDepartmentID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluationMethod','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluationReserveFinYTD','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluationReserveFinYTDDepr','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluationReserveFinYTDImp','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluationReserveOpening','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluedAmount','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('RevaluedBy','T_STRING');
--INSERT INTO  transactions.field (name,type) VALUES ('RevImpairmentAll','T_MONEY'); merged --> Impairment
--INSERT INTO  transactions.field (name,type) VALUES ('RevImpairmentDate', 'T_DATETIME'); merged --> Impairment   RULE: EffectiveDate = MAX(ImpairmentDate,RevImpairmentDate)
--INSERT INTO  transactions.field (name,type) VALUES ('RevImpairmentFinYTD','T_MONEY'); merged --> Impairment
INSERT INTO  transactions.field (name,type) VALUES ('Impairment','T_MONEY'); -- result of merge --> Impairment
INSERT INTO  transactions.field (name,type) VALUES ('RiskExposure','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('RUL','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('SuburbName','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('SupplierID','T_STRING');
--INSERT INTO  transactions.field (name,type) VALUES ('TakeOnDate','T_DATETIME'); merged --> Additions RULE: EffectiveDate = TakeOnDate  to be confirmed
INSERT INTO  transactions.field (name,type) VALUES ('TransferCost','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('TransferDepr','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('TreasuryCode','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('UseStatusID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('UtilisationGrade','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('UtilisationGrade_CG','T_SLONG');
--INSERT INTO  transactions.field (name,type) VALUES ('ValueChangeFinYTD','T_MONEY'); surely this can be calculated ????
INSERT INTO  transactions.field (name,type) VALUES ('ValueInUse','T_MONEY');
INSERT INTO  transactions.field (name,type) VALUES ('WardNr','T_SLONG');
INSERT INTO  transactions.field (name,type) VALUES ('WIP_Project_ID','T_STRING');
INSERT INTO  transactions.field (name,type) VALUES ('YearConstruct','T_SLONG');

-- ===================================

UPDATE transactions.field SET fqn =  'public.financials.additions'					WHERE name =   'Additions';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'AdditionsFundTypeID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'AdditionsNature';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'AssetCustodianID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'AssetFacilityId';

UPDATE transactions.field SET fqn =  NULL WHERE name =   'AssetMoveableID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'AssetOwnerID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'BOQPath';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'CashGenerating';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ClientAssetID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ComponentDesc';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ComponentID';
UPDATE transactions.field SET fqn =  'public.lifecycle.condition_grade' 				WHERE name =   'ConditionGrade';
UPDATE transactions.field SET fqn =  'public.lifecycle.condition_grade_cg' 			WHERE name =   'ConditionGrade_CG';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'CostCentreCode';
UPDATE transactions.field SET fqn =  'public.financials.cost'						WHERE name =   'Cost';
UPDATE transactions.field SET fqn =  'public.financials.crc' 						WHERE name =   'CRC';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'CRC_Adjusted';
UPDATE transactions.field SET fqn =  'public.lifecycle.criticality_grade' 			WHERE name =   'CriticalityGrade';
UPDATE transactions.field SET fqn =  'public.lifecycle.criticality_grade_cg'			WHERE name =   'CriticalityGrade_CG';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DateLastFinMonth';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DateLastRenewed';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DebtSecurityApplicable';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DepreciationMethodID';
UPDATE transactions.field SET fqn =  'public.financials.depreciation' 				WHERE name =   'Depreciation';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DisposalMethodID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DisposalProceedCost';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'DisposalProfitLoss';
UPDATE transactions.field SET fqn =  'public.lifecycle.eul_adjusted'					WHERE name =   'EUL_Adjusted';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'Extent';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'FairValue';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'FairValueLessCostSell';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'FuncLocPath';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'FundingSourceID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'FundingTypeID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'GeneralLedgerCode';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ImpairmentReason';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'InsuranceCover';
UPDATE transactions.field SET fqn =  'public.location.latitude' 						WHERE name =   'Latitude';
UPDATE transactions.field SET fqn =  'public.location.address'                       WHERE name =   'LocationAddress';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'LocationSGcode';
UPDATE transactions.field SET fqn =  'public.location.stand'                         WHERE name =   'LocationStand';
UPDATE transactions.field SET fqn =  'public.location.longitude' 					WHERE name =   'Longitude';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'MaintenanceExpenditure';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'MapFeatureID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'MeasurementModel';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'MeasurementModelID';
UPDATE transactions.field SET fqn =  'public.lifecycle.ops_cost_grade'				WHERE name =   'OpsCostGrade';
UPDATE transactions.field SET fqn =  'public.lifecycle.ops_cost_grade_cg'			WHERE name =   'OpsCostGrade_CG';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'OwnedLeasedID';
UPDATE transactions.field SET fqn =  'public.lifecycle.performance_grade'			WHERE name =   'PerformanceGrade';
UPDATE transactions.field SET fqn =  'public.lifecycle.performance_grade_cg' 		WHERE name =   'PerformanceGrade_CG';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ProvisionAdjust';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ProvisionOpening';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RegionName';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RespDepartmentID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluationMethod';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluationReserveFinYTD';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluationReserveFinYTDDepr';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluationReserveFinYTDImp';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluationReserveOpening';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluedAmount';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RevaluedBy';
UPDATE transactions.field SET fqn =  'public.financials.impairment'					WHERE name =   'Impairment';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'RiskExposure';
UPDATE transactions.field SET fqn =  'public.lifecycle.rul' 							WHERE name =   'RUL';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'SuburbName';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'SupplierID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'TransferCost';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'TransferDepr';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'TreasuryCode';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'UseStatusID';
UPDATE transactions.field SET fqn =  'public.lifecycle.ops_utilisation_grade'		WHERE name =   'UtilisationGrade';
UPDATE transactions.field SET fqn =  'public.lifecycle.ops_utilisation_grade_cg'		WHERE name =   'UtilisationGrade_CG';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'ValueInUse';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'WardNr';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'WIP_Project_ID';
UPDATE transactions.field SET fqn =  NULL WHERE name =   'YearConstruct';