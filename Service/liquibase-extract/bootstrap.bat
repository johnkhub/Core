call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase --changeLogFile=changelog_public.json --classpath=postgresql-42.2.5.jar --defaultSchemaName=public --url=jdbc:postgresql://localhost:5432/core12feb_2?currentSchema=public --username=imqs --password=1mq5p@55w0rd changelogSync
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase --changeLogFile=changelog_audit.json --classpath=postgresql-42.2.5.jar --defaultSchemaName=public --url=jdbc:postgresql://localhost:5432/core12feb_2?currentSchema=audit --username=imqs --password=1mq5p@55w0rd changelogSync
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase --changeLogFile=changelog_asset.json --classpath=postgresql-42.2.5.jar --defaultSchemaName=public --url=jdbc:postgresql://localhost:5432/core12feb_2?currentSchema=asset --username=imqs --password=1mq5p@55w0rd changelogSync
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase --changeLogFile=changelog_access_control.json --classpath=postgresql-42.2.5.jar --defaultSchemaName=public --url=jdbc:postgresql://localhost:5432/core12feb_2?currentSchema=access_control --username=imqs --password=1mq5p@55w0rd changelogSync
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase --changeLogFile=changelog_dtpw.json --classpath=postgresql-42.2.5.jar --defaultSchemaName=public --url=jdbc:postgresql://localhost:5432/core12feb_2?currentSchema=dtpw --username=imqs --password=1mq5p@55w0rd changelogSync

