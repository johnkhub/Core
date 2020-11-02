Database Schema Management
==========================

* There are Generic schemas and client specific schema
* There is a changelog for each schema
* Note the split into the common schemas and a *client specific* schema. `dtpw` is an example of this.
* The sub-folders contains sql scripts that are executed by Liquibase
* Refer to commandline reference for how the application manages the schemas
* All n_changelog_xxx.json files are processed where `n` is a digit that control the ordering in which files are loaded; lowest number first
* Schemas are created based on the names of the changelog files so `4_changelog_access_control.json` would result in `access_control` being created
* Master data is loaded from sql scripts
* Standard view definitions are loaded from sql scripts