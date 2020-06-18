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
 * There were asset entries in both envelope and facility for the same asset - how can we impose a db constraint avoid this?   
 * The DTPW view does not seem to show everything it should - maybe when there are holes in the path? Definitely an issue with Land Parcels
 * Especially the LPI import had lost of issue with whitespace around values and space/empty strings instead of blanks
 * Need a way to DELETE entries and to SCRIPT it
 
 ### In progress
  * Need a mechanism to find garbage in the database
      * Assets linked to multiple sub-types as mentioned above
      * The link invalid code reports a lot if not all landparcels as being broken - so there is something wrong there      


TEST UPDATE EXTERNAL LINK
FIRST COLUMN IS LEVEL

NEED TO MAP OUT DATA WORK THAT IS BLOCKED AND HOW THAT BLOCKS DEVELOPMENT
NEED TO MAP OUT WHAT COMPONENTS I NEED TO LOOK AT
    REPORTING
    AUTH
    FAR
    IoT / CONVERSION
    AUDIT
WHEN ARE WE DOING THE ROADMAP / PRIORITISATION GLOBAL EXERCISE
MASH & VOSLOO WRITEUPS - REQUIRED TO THEN IMPOSE IN DESIGN DECISIONS AAND EVANGELESIZE
LACK OF ENGAGEMENT IN PLANNING

DTPW Data
==========


* The import file has provision for both a suburb name and suburb code. Suburb name is ignored though as we make use of the code to address a lookup table. This means that some suburbs where we had only the names filled in (presumable they did not come from the FAR @Burgert Malan?) that were not imported. Action: we should examine all of the import files and identify where only the name is specified and decide how to assign codes to the same
* We should also be aware of the fact that the reporting view is a persisted one, so it needs to be refreshed when data changes. This needs to happen periodically and so by definition the report view will lag rela-time updates by whatever the update period is.@David Jacobs, I'm also including you here as I don't know what is required/impacted in reports.
*broken data: paths



DATA FIXES
First have extra columns / parents nullable then apply secondary fix to make them non nullable after lookups have been fixed

Adding an asset sub-type
---------------------------
1. Create a DTO class
2. Add the DTO to the named list of @JsonSubTypes in CoreAssetDto
3. Create a Domain model class
4. Modify AssetFactory
5. Modify Importer

Query API
---------

1. Must be able to specify operators and values against fields
2. Must be able to specify and, or , not
3. NULL or EMPTY?
4. Must be able to automatically serialize to correct asset sub-class
5. OFFSET / LIMIT
6. GROUP BY 
7. ORDER BY ASC / DESC

 