NOTES
======

Imports
-------
 
 1. If asset_id column set, then we do an update?
 2. Add asset_report_view to liquibase
    * Add a view to show the num links of a specific external type
    * Use this view to add counts of emis to view - **this can be gotten from the report view**
    * Add a view to show the num tags of a specific type
 
 Known problems that came out
 ---------------------------- 
 
 * After creating but before restoring the db, we need to install the extensions manually
 
 ### In progress
      
FIRST COLUMN IS LEVEL


NEED TO MAP OUT WHAT COMPONENTS I NEED TO LOOK AT
    REPORTING
    AUTH
    FAR
    IoT / CONVERSION
    AUDIT



DTPW Data
==========



* We should also be aware of the fact that the reporting view is a persisted one, so it needs to be refreshed when data changes. This needs to happen periodically and so by definition the report view will lag rela-time updates by whatever the update period is.
@David Jacobs, I'm also including you here as I don't know what is required/impacted in reports.
*broken data: paths


Update the IMPORT.md file with te executible for imports once this is finalised

Query API
---------

3. NULL or EMPTY?
4. Must be able to automatically serialize to correct asset sub-class


Address -- but also need specialised api for this
need hastags function in query language
Organisational
Better parsing/error reporting in query language


Problems
 --------
 
 functions for validating paths and subclasses don't "restore"
 asset_link
 PK = (asset_id, external_id)
 IDX = (external_id_type, asset_id, external_id)
 
 Change to
 
 PK = (asset_id, external_id)
 IDX = (external_id_type, external_id) UNIQUE