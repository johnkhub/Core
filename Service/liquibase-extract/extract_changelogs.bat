del *.json
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.public.properties --changeLogFile=changelog_public.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.dtpw.properties --changeLogFile=changelog_dtpw.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.asset.properties --changeLogFile=changelog_asset.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.access_control.properties --changeLogFile=changelog_access_control.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.audit.properties --changeLogFile=changelog_audit.json  generateChangeLog

call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.public_pcs.properties --changeLogFile=changelog_public_pcs.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.dtpw_pcs.properties --changeLogFile=changelog_dtpw_pcs.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.asset_pcs.properties --changeLogFile=changelog_asset_pcs.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.access_control_pcs.properties --changeLogFile=changelog_access_control_pcs.json generateChangeLog
call C:\Workspaces\Tools\liquibase\liquibase-3.8.7\liquibase  --defaultsFile=liquibase.audit_pcs.properties --changeLogFile=changelog_audit_pcs.json  generateChangeLog