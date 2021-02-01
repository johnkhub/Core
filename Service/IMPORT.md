Import
======

 **CAVEAT:** Under normal circumstances,THERE IS NO WAY TO DELETE DATA VIA AN IMPORT, so: 
 * There is no way to remove a specific value for a specific asset
 * By extension, it is also impossible to move an attribute from one Asset to another e.g. moving an EMIS number
 * An Asset may be marked as inactive, but it will remain in the database
 
 The server may be stopped though and run using the `admin` profile. This makes it possible to delete assets via the importer.
 Refer to the [configuration documentation](CONFIG.md).

Download page
-------------

The service provides a mechanism to download the version of the importer appropriate to the current version of the service.
In a browser, navigate to http://<host>[:port]/download/importer/index.html.

E.g. `https://pprd1.assetworld.co.za/download/importer/index.html`

This page provides download links to the files required by the importer. 
>**NOTE** that some details in the configuration files must be updated to match the configuration of your server before running the importer.


Data format
-----------

* Data can be split over as many files as you like as long as the format is correct
* The first column in the file must be the asset type
* The ordering of other columns does not matter
 
 
 Importing Assets
 ----------------
 
### Process ###
 
* For each file, the import system will make one pass through the system for each asset type
* Extra passes over and above these are made for extra data such as external identifiers
* An exception file, containing the rows that failed to import, is generated for each pass

> This approach is of course much slower than a single pass, but it makes the implementation so much easier as
> this makes it easier to ensure that say Envelopes, are imported before asset types that are children of the Envelope
> It also makes it easier to use the same DTOs within the service and the Importers 

* Importers **shall** use the REST endpoints implemented by the service to implement imports.  This ensures that:
    * Business rules are applied consistently
    * Audit and other logging is consistent
    * Is arguably less word to implement
    * Arguably test coverage is better by exercising the same code paths 
    

Link Assets to Landparcels
--------------------------

To link Assets to Landparcels, we use a simple csv file containing two columns 
`asset_id` the UUID of the asset to link to the landparcel and `landparcel_asset_id` the Asset UUID of the Landparcel.  

> Note: All of the Assets and Landparcels must already exist in the system.

 Commandline
 -------------

`<import command> <config file> <type> [<type specific parameters>]`

Where `<import command>` would look like 
```
java 
  -Dlogback.configurationFile="logback-asset-core-service.groovy" 
  -classpath asset-core-service-jar-with-dependencies.jar za.co.imqs.coreservice.imports.Importer
  "import_config.json" $1 $2 $3 $4
```
The above is also packaged in a `.bat` and `.sh` file: `import.bat` and `import.sh`

|type|parameter|description|
|----|---------|-----------|
|lookups|type|The type of lookup (as defined in the `public.kv_type` table)|
|assets|flags|`FORCE_INSERT`,`FORCE_CONTINUE`,`FORCE_UPSERT` (see below)|
|delete|flags|Normally this will mark Assets as inactive - while not modifying any data. To truly delete an Asset specify `HARD_DELETE`(see below)|
|asset_to_landparcel|none||


|flag|description|
|----|------------|
|`FORCE_INSERT`|Mostly relevant to developers. Will force the insert of a row even if it contains an asset_id. This is useful to import an export from another system while retaining the asset_id values.|
|`FORCE_CONTINUE`|Instead of failing on the first error the import will continue instead writing all failing rows to an exception csv file. **NOTE:** Incorrectly formatted CSV files will fail on first error|
|`FORCE_UPSERT`|If set the importer will check if the asset exists and then perform an UPDATE else it will INSERT|
|`HARD_DELETE`|Completely deletes an Asset from the datavase. Requires server to run in `admin` mode.|

>Flags are comma (`,`) separated

### Examples ###
 Import assets writing failures to exception files.
 
 `import "import_config.json" assets 43703_43734_5761_UPDATE FL.CSV FORCE_CONTINUE`
 
 Import lookup district lookups
 
 `import "import_config.json" lookups "ref_district.csv" DISTRICT`
 


### Configuration File
The file contains the URLs to the Core and Auth services, as well as the username and password 
required to log onto the system

E.g.
```
{
  "authUrl" : "http://localhost/auth2/login",
  "serviceUrl": "http://localhost:8669",
  "loginUsername" : "dev",
  "loginPassword": "dev"
}
```
 
Exception files
---------------- 
 
 An exception file is generated for each type of asset being imported. The file is in exactly 
 the same format as the imports allowing you to make updates in the file and use it as a source to import from. 
