USE [master]
GO


EXEC master.dbo.sp_dropserver @server=N'BCM_CORE', @droplogins='droplogins'
GO


EXEC master.dbo.sp_addlinkedserver @server = N'BCM_CORE', @srvproduct=N'PostgreSQL', @provider=N'MSDASQL', @datasrc=N'Core'
EXEC master.dbo.sp_addlinkedsrvlogin @rmtsrvname=N'BCM_CORE',@useself=N'False',@locallogin=NULL,@rmtuser=N'imqs',@rmtpassword='1mq5p@55w0rd'
EXEC master.dbo.sp_addlinkedsrvlogin @rmtsrvname=N'BCM_CORE',@useself=N'False',@locallogin=N'sa',@rmtuser=N'imqs',@rmtpassword='1mq5p@55w0rd'

GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'collation compatible', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'data access', @optvalue=N'true'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'dist', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'pub', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'rpc', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'rpc out', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'sub', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'connect timeout', @optvalue=N'0'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'collation name', @optvalue=null
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'lazy schema validation', @optvalue=N'false'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'query timeout', @optvalue=N'0'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'use remote collation', @optvalue=N'true'
GO

EXEC master.dbo.sp_serveroption @server=N'BCM_CORE', @optname=N'remote proc transaction promotion', @optvalue=N'true'
GO


