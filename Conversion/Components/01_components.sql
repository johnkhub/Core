--
-- MS SQL stored procedures to act as API for writes to the postgres database
--
ALTER PROCEDURE [dbo].[sp_add_asset]
(
	@asset_id uniqueidentifier, 
	@asset_type_id varchar(10),
	@grap_path varchar(40),
	@adm_path varchar(40)
)
AS
BEGIN
	PRINT 'INSERT INTO  asset (asset_id, asset_type_code, adm_path, grap_path) VALUES ('''+CONVERT(VARCHAR(40),@asset_id)+''','''+CONVERT(VARCHAR(40),@asset_type_id)+''','''+ @adm_path+''','''+ @grap_path+''');'
END;
GO


ALTER PROCEDURE [dbo].[sp_link_asset_to_external]
(
	@asset_id uniqueidentifier, 
	@external_id varchar(40), 
	@external_id_type uniqueidentifier 
)
AS
BEGIN
	PRINT 'INSERT INTO asset_link (asset_id, external_id, external_id_type) VALUES ('''+CONVERT(VARCHAR(40),@asset_id)+''','''+CONVERT(VARCHAR(40),@external_id)+''','''+CONVERT(VARCHAR(40),@external_id_type)+''');'
END;
GO

ALTER PROCEDURE [dbo].[sp_link_asset_to_facility]
(
	@asset_id uniqueidentifier, 
	@facility_code varchar(10)
)
AS
BEGIN	
	PRINT 'INSERT INTO asset_facility (asset_id, facility_code) VALUES ('''+CONVERT(VARCHAR(40),@asset_id)+''','''+@facility_code+''');'
END;
GO

ALTER PROCEDURE sp_split_fqn 
(
	@fqn VARCHAR(120),
	@schema VARCHAR(40) OUTPUT,
	@table VARCHAR(40) OUTPUT,
	@field VARCHAR(40) OUTPUT
) AS
BEGIN
		SET @schema = LEFT(@fqn,CHARINDEX('.',@fqn)-1)			-- copy upto first '.'
		SET @fqn = SUBSTRING(@fqn,CHARINDEX('.',@fqn)+1, 100)	-- set @fqn to remainder of string after first '.'
		SET @table = LEFT(@fqn,CHARINDEX('.',@fqn)-1)
		SET @fqn = SUBSTRING(@fqn,CHARINDEX('.',@fqn)+1, 100)
		SET @field= @fqn										-- only field part remains
END
GO

ALTER PROCEDURE [dbo].[sp_process_money_field]
(
	@asset_id varchar(40), 
	@batch_id uniqueidentifier, 
	@transaction_type varchar(10), 
	@field varchar(20), 
	@effective_date DateTime, 
	@value decimal(19,6), 
	@reason varchar = NULL,
	@flags int = 0
)
AS
BEGIN
	DECLARE @uuid AS uniqueidentifier = NEWID()
	IF @flags & 1 = 0
	BEGIN
		PRINT 'INSERT INTO  transaction (transaction_id, transaction_type_code, asset_id, batch_id, submit_date, effective_date, reason, field, amount)	VALUES ('''+
			CONVERT(varchar(40), @uuid) + ''',' + 
			''''+ @transaction_type + '''' + ','+ 
			''''+ @asset_id + ''''+ ','+ 
			''''+ CONVERT(varchar(40), @batch_id) + '''' + ','+
			''''+ CONVERT(varchar(40),CURRENT_TIMESTAMP) + '''' +','+ 
			''''+ CONVERT(varchar(40),@effective_date) + '''' +  ','+ 
			'''Initial load''' +','+ 
			'''' + @field + '''' + ',' +
			'''' + CONVERT(varchar(40),@value) + '''' +
			');'
	END	
	
	DECLARE @fqn AS VARCHAR(120) = (SELECT fqn FROM [BCM_CORE].[Core].[public].[field] WHERE name = @field)
	IF (@fqn IS NOT NULL) AND (@flags & 2 = 0)
	BEGIN
		DECLARE @schema_part VARCHAR(40)
		DECLARE @table_part VARCHAR(40)
		DECLARE @field_part VARCHAR(40)
		EXEC sp_split_fqn @fqn, @schema_part OUTPUT , @table_part OUTPUT, @field_part OUTPUT
		PRINT 'UPDATE ' + @schema_part+'.'+@table_part + ' SET ' + @field_part + ' = ''' + CONVERT(VARCHAR(40),@value) + ''' WHERE asset_id = ''' + @asset_id +''';'
	END
END;
GO

ALTER PROCEDURE [dbo].[sp_process_attribute]
	@asset_id varchar(40), 
	@batch_id uniqueidentifier, 
	@transaction_type varchar(10), 
	@field varchar(20), 
	@effective_date DateTime, 
	@value varchar(40), 
	@reason varchar = NULL,
	@flags int = 0
AS
BEGIN
	DECLARE @uuid AS uniqueidentifier = NEWID()

	--- START Move to scalar function
	DECLARE @type AS VARCHAR(10) = (SELECT type FROM [BCM_CORE].[Core].[public].[field] WHERE name = @field)
	IF @type IS NULL
	BEGIN
		DECLARE @msg AS VARCHAR(120) = 'No type mapping for field "' + @field +'"';
		THROW 51000, @msg, 1
	END

	DECLARE @target VARCHAR(20) = 
	CASE @type
		WHEN 'T_SLONG' THEN '"delta_T_SLONG"'
		WHEN 'T_ULONG' THEN '"delta_T_ULONG"'
		WHEN 'T_BOOLEAN' THEN '"delta_T_BOOLEAN"'
		WHEN 'T_STRING' THEN '"delta_T_STRING"'
		WHEN 'T_POLYGON' THEN '"delta_T_POLYGON"'
		WHEN 'T_DATETIME' THEN '"delta_T_STRING"'
		WHEN 'T_MONEY' THEN 'amount'
		ELSE 
			'INVALID'
	END
		
	IF @target = 'INVALID'
	BEGIN
		SET @msg = 'Unknown type "' + @type + '" processing field "' + @field +'"';
		THROW 51000, @msg, 1
	END
	--- END Move to scalar function

	BEGIN TRY		
		IF @type = 'T_SLONG' OR @type = 'T_ULONG'
		BEGIN
			SET @value = CONVERT(int, CONVERT(numeric(19,4),@value))
		END
		
		/*
		SET @value = CASE @type
		WHEN 'T_SLONG' THEN 
			CONVERT(int, CONVERT(numeric(19,4),@value))
		WHEN 'T_ULONG' THEN 
			CONVERT(int, CONVERT(numeric(19,4),@value))
		WHEN 'T_BOOLEAN' THEN 
			CONVERT(bit, @value)
		WHEN 'T_STRING' THEN 
			@value
		WHEN 'T_POLYGON' THEN 
			@value
		WHEN 'T_DATETIME' THEN 
			@value
		WHEN 'T_MONEY' THEN 
			CONVERT(numeric(19,4),@value)
		END
		*/
	END TRY
	BEGIN CATCH
		SET @msg = ERROR_MESSAGE() + 'Value: '+@value+'. Target meta-type: ' + @type;
		THROW 51000, @msg, 1
	END CATCH
	
	
	IF @flags & 1 = 0
	BEGIN
		PRINT 'INSERT INTO  transaction (transaction_id, transaction_type_code, asset_id, batch_id, submit_date, effective_date, reason, field, '+@target+')	VALUES ('''+
			CONVERT(varchar(40), @uuid) + ''',' + 
			''''+ @transaction_type + '''' + ','+ 
			''''+ @asset_id + ''''+ ','+ 
			''''+ CONVERT(varchar(40), @batch_id) + '''' + ','+
			''''+ CONVERT(varchar(40),CURRENT_TIMESTAMP) + '''' +','+ 
			''''+ CONVERT(varchar(40),@effective_date) + '''' +  ','+ 
			'''Initial load''' +','+ 
			'''' + @field + '''' + ',' +
			'''' + @value + '''' +
		');'
	END

	DECLARE @fqn AS VARCHAR(120) = (SELECT fqn FROM [BCM_CORE].[Core].[public].[field] WHERE name = @field)
	IF (@fqn IS NOT NULL) AND (@flags & 2 = 0)
	BEGIN
		DECLARE @schema_part VARCHAR(40)
		DECLARE @table_part VARCHAR(40)
		DECLARE @field_part VARCHAR(40)
		EXEC sp_split_fqn @fqn, @schema_part OUTPUT , @table_part OUTPUT, @field_part OUTPUT
		PRINT 'UPDATE ' + @schema_part+'.'+@table_part + ' SET ' + @field_part + ' = ''' + @value + ''' WHERE asset_id = ''' + @asset_id +''';'
	END
END;
GO