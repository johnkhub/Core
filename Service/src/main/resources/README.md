Database Schema Management
==========================

* There is a changelog for each schema
* Note the split into the common schemas and a *client specific* schema. `dtpw` is an example of this.
* The `common` subfolder contains sql scripts that are executed by Liquibase
* There are also client specific schema subfolders for use by the client specific changelog


`bootstrap.bat`
---------------

This forces a liquibase into a known state and I used this to retrofit liquibase onto an existing hand-crafted DTPW database.
Steps:
1. Clear out existing liquibase changelog tables
2. Run the batch file

It will create the necessary liquibase changelog table entries to indicate that the supplied changelogs represent the current state of the database.

**NOTE:** You will need to edit the batch file for execution on your system.