--
-- Iterate over all Components
--
SET NOCOUNT ON
DELETE FROM [BCM_CORE].[Core].[public].[asset_link];
DELETE FROM [BCM_CORE].[Core].[public].[asset];
DELETE FROM [BCM_CORE].[Core].[public].[transaction];
DELETE FROM [BCM_CORE].[Core].[public].[transaction_batch];
DELETE FROM [BCM_CORE].[Core].[public].[location];
DELETE FROM [BCM_CORE].[Core].[public].[asset_facility];
DELETE FROM [BCM_CORE].[Core].[public].[lifecycle];
DELETE FROM [BCM_CORE].[Core].[public].[financials];


UPDATE AssetRegisterIconFin2019 SET DirtyFlag = 1;

DECLARE @total AS bigint = 0
DECLARE @idx AS bigint = 0;

DECLARE @flags int = 0

DECLARE component_cursor cursor FORWARD_ONLY
FOR SELECT TOP 5000 ComponentID FROM AssetRegisterIconFin2019
OPEN component_cursor

DECLARE @component_id varchar(40)
DECLARE @batch_id uniqueidentifier

	
FETCH NEXT FROM component_cursor INTO @component_id
WHILE @@FETCH_STATUS = 0 BEGIN
	BEGIN TRY
		--BEGIN TRANSACTION process_component

        SET @idx = @idx+1;
		IF @idx % 1000 = 0
		PRINT '-- '+CONVERT(VARCHAR(20), @idx*100 / @total) + ' % complete' 
	
		SET @batch_id = NEWID()
		EXEC sp_process_component @component_id, @batch_id, @flags
			
		UPDATE AssetRegisterIconFin2019 SET DirtyFlag = 0 WHERE CURRENT OF component_cursor
		--COMMIT TRANSACTION process_component
	END TRY

	BEGIN CATCH
		--IF @@TRANCOUNT > 0 BEGIN
		--	ROLLBACK TRANSACTION process_component
		--END
		SELECT
			ERROR_NUMBER() AS ErrorNumber,
			ERROR_SEVERITY() AS ErrorSeverity,
			ERROR_STATE() AS ErrorState,
			ERROR_PROCEDURE() AS ErrorProcedure,
			ERROR_LINE() AS ErrorLine,
			'Handling Component '+ @component_id + ' : ' + ERROR_MESSAGE() AS ErrorMessage
	END CATCH 

	FETCH NEXT FROM component_cursor INTO @component_id
END
CLOSE component_cursor
DEALLOCATE component_cursor


