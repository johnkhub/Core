--
-- Changing year: Serach and replace 2019 with 2020 etc.
--

ALTER PROCEDURE [dbo].[sp_process_component]
	@component_id varchar(40), 
	@batch_id uniqueidentifier,
	@flags int = 0
AS
BEGIN
	-------------------------------------------------------
	-- Compute the path strings we need to add to the Asset
	-------------------------------------------------------

	
-- Fin Path
	DECLARE @finpath varchar(40)
	SET @finpath = (SELECT AccountingGroupId FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	SET @finpath = @finpath + '-' + (SELECT AssetCategORyId FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	SET @finpath = @finpath + '-' + (SELECT AssetSubCategORyId FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	SET @finpath = @finpath + '-' + (SELECT AssetGroupId FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	SET @finpath = @finpath + '-' + (SELECT AssetTypeId FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	SET @finpath = @finpath + '-' + (SELECT ComponentType FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id)
	
	
	-- BOQ Path
	DECLARE @admpath varchar(40) = 
	(
		SELECT BOQPath FROM AssetPolicyVAR WHERE 
			FinYear = 2019 AND 
			ComponentTypeId = (SELECT ComponentType FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) AND
			DescriptorType = (SELECT DescriptORType FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) AND
			
			(
				DescriptorClass = (SELECT DescriptorClass FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) 
				OR
				((SELECT DescriptorClass FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) IS NULL AND DescriptorClass = 'NULL')
			)
			
			AND

			(
				DescriptorSize = (SELECT DescriptORSize FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) 
				OR
				((SELECT DescriptorSize FROM AssetRegisterIconFin2019 WHERE ComponentId = @component_id) IS NULL AND DescriptorSize IS NULL)
			)
	
	)
	
	----------------------------------------------------------------
	-- Add the asset and link the V8 asset id to the v6 component id
	----------------------------------------------------------------
	DECLARE @asset_id uniqueidentifier = NEWID()
	IF @finpath IS NULL
		THROW 61000, 'Financial path is NULL!', 1

	IF @admpath IS NULL 
		RETURN

	EXEC sp_add_asset @asset_id, 'DUMMY', @finpath, @admpath
	EXEC sp_link_asset_to_external @asset_id, @component_id, 'c6a74a62-54f5-4f93-adf3-abebab3d3467' -- Link to : Client Asset Id (link tables)
    	
	---------------------------------------------------------------
	-- Transactions
	---------------------------------------------------------------
	DECLARE @effective_date datetime
	DECLARE @amount decimal(19,6)
	DECLARE @reason varchar

	IF @flags & 1 = 0
	BEGIN
		PRINT 'INSERT INTO transaction_batch (batch_id) VALUES ('''+CONVERT(varchar(40),@batch_id)+''');'
	END
	
	-- The tables providing the current state of the register
	PRINT 'INSERT INTO lifecycle (asset_id) VALUES (''' + CONVERT(varchar(40),@asset_id) +''');' 
	PRINT 'INSERT INTO financials (asset_id) VALUES (''' + CONVERT(varchar(40),@asset_id) +''');' 

	-- Additions
	SET @effective_date  = (SELECT TakeOnDate FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	SET @amount = (SELECT AdditionsFinYtd FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	if (@effective_date IS not NULL) 
		EXEC sp_process_money_field @asset_id, @batch_id, 'ADDITION', 'Additions', @effective_date, @amount, null, @flags
 
  
	--  Depreciation
	SET @effective_date = CURRENT_TIMESTAMP
	SET @amount = (SELECT COALESCE(DepreciationOpening,0)+COALESCE(DepreciationFinYtd,0) FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	IF (@effective_date IS NOT NULL) 
		EXEC sp_process_money_field @asset_id, @batch_id, 'DEPRECIATE', 'Depreciation', @effective_date, @amount, null, @flags


	-- Impairment
	SET @effective_date = (SELECT CASE WHEN ImpairmentDate >= RevImpairmentDate THEN ImpairmentDate ELSE RevImpairmentDate END	FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	SET @amount = (SELECT COALESCE(RevImpairmentAll,0)+COALESCE(RevImpairmentFinYTD,0)+COALESCE(ImpairmentFinYTD,0)+COALESCE(ImpairmentAll,0) FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	SET @reason = (SELECT CASE WHEN ImpairmentDate >= RevImpairmentDate THEN ImpairmentReason ELSE RevImpairmentReason END	FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	
	IF (@effective_date IS NOT NULL) 
		EXEC sp_process_money_field @asset_id, @batch_id, 'IMPAIR', 'Impairment', @effective_date, @amount, @reason, @flags

	
	SET @effective_date = CURRENT_TIMESTAMP
	EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'adm_path', @effective_date, @admpath, null, @flags
	EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'grap_path', @effective_date, @finpath, null, @flags
  

    -- Life cycle values
    DECLARE @value AS varchar(40) = CONVERT(varchar(40), (SELECT ConditionGrade FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'ConditionGrade', @effective_date, @value, null, @flags

    SET @value = CONVERT(varchar(40),(SELECT CriticalityGrade FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'CriticalityGrade', @effective_date, @value, null, @flags

    SET @value = CONVERT(varchar(40),(SELECT OpsCostGrade FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'OpsCostGrade', @effective_date, @value, null, @flags

	SET @value = CONVERT(varchar(40),(SELECT UtilisationGrade FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'UtilisationGrade', @effective_date, @value, null, @flags

	SET @value = CONVERT(varchar(40),(SELECT ConditionGrade_CG FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'ConditionGrade_CG', @effective_date, @value, null, @flags

	SET @value = CONVERT(varchar(40),(SELECT CriticalityGrade_CG FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'CriticalityGrade_CG', @effective_date, @value, null, @flags

    SET @value = CONVERT(varchar(40),(SELECT OpsCostGrade_CG FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'OpsCostGrade_CG', @effective_date, @value, null, @flags

    SET @value = CONVERT(varchar(40),(SELECT UtilisationGrade_CG FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'UtilisationGrade_CG', @effective_date, @value, null, @flags


    -- Facility
	DECLARE @facility_id AS varchar(40) = (SELECT F.AssetFacilityCode FROM AssetRegisterIconFin2019 R JOIN AssetRegisterFacility F ON R.AssetFacilityName = F.AssetFacilityName WHERE componentid = @component_id )
	IF @facility_id IS NOT NULL
	BEGIN	
		SET @facility_id = REPLICATE('0',10-LEN(RTRIM(@facility_id))) + @facility_id
		EXEC sp_link_asset_to_facility @asset_id, @facility_id
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'AssetFacilityId', @effective_date, @facility_id, null, @flags
	END

	-- Location
	SET @value = CONVERT(varchar(40),(SELECT Latitude FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'Latitude', @effective_date, @value, null, @flags


	SET @value = CONVERT(varchar(40),(SELECT Longitude FROM AssetRegisterIconFin2019 WHERE componentid = @component_id))
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'Longitude', @effective_date, @value, null, @flags
	

	SET @value = (SELECT LocationAddress FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'LocationAddress', @effective_date, @value, null, @flags

	SET @value = (SELECT CONVERT(VARCHAR(10),WardNr) FROM AssetRegisterIconFin2019 WHERE componentid = @component_id)
	IF @value IS NOT NULL
		EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'WardNr', @effective_date, @value, null, @flags

	/*
	SET @amount = (SELECT Building_Name FROM AssetRegisterIconFin2019 A JOIN AssetRegisterIconMove M ON A.componentid = M.componentid  WHERE A.componentid = @component_id)
	EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'Building_Name', @effective_date, @amount, null, @flags

	SET @amount = (SELECT [Floor] FROM FROM AssetRegisterIconFin2019 A JOIN AssetRegisterIconMove M ON A.componentid = M.componentid  WHERE A.componentid = @component_id)
	EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'Floor', @effective_date, @amount, null, @flags

	SET @amount = (SELECT Room_Number FROM FROM AssetRegisterIconFin2019 A JOIN AssetRegisterIconMove M ON A.componentid = M.componentid  WHERE A.componentid = @component_id)
	EXEC sp_process_attribute @asset_id, @batch_id, 'DATALOAD', 'Room_Number', @effective_date, @amount, null, @flags
	*/

	
	/*
	INSERT INTO  field (name,type) VALUES ('SuburbName','T_STRING');
	INSERT INTO  field (name,type) VALUES ('WardNr','T_SLONG');
	INSERT INTO  field (name,type) VALUES ('RegionName','T_STRING');

	-- JOIN ON AssetRegisterMove !
	INSERT INTO  field (name,type) VALUES ('Building_Name','T_STRING');
	INSERT INTO  field (name,type) VALUES ('Floor','T_STRING');
	INSERT INTO  field (name,type) VALUES ('Room_Number','T_STRING');

	
	Region
		Suburb
			Stand
				Building
					Floor
						Room

    Ward (not directly mapped to above?)

*/
/*
	,[Asset_Barcode_Nr]
	,[Serial_Number]
    ,[Room_Barcode_Nr]
    ,[Fleet_Number]
	,[Treasury_Code]
    
  */  


END
