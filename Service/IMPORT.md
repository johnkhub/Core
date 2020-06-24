Import
======

Data format
-----------

* Data can be split over as many files as you like as long as the format is correct
* The first column in the file must be the asset type
* The ordering of other column does not matter
 
Process
-------
 
* For each file, the import system will make one pass through the system for each asset type
* Extra passes over and above these are made for extra data such as external identifiers
* An exception file, containing the rows that failed to import, is generated for each pass

> This approach is of course much slower than a single pass, but it makes the implementation so much easier as
> this makes it easier to ensure that say Envelopes, are imported before asset types that are children of the Envelope
>It also makes it easier to use the same DTOs within the service and the Importers 

* Importers **shall** use the REST endpoints implemented by the service to implement imports.  This ensures that:
    * Business rules are applied consistently
    * Audit and other logging is consistent
    * Is arguably less word to implement
    * Arguably test coverage is better by exercising the same code paths 
    
 Configuration
 -------------

`importer <config file> <type> [<type specific parameters>]`

|type|parameter|description|
|----|---------|-----------|
|lookups|type|The type of lookup (as defined in the `public.kv_type` table)|
|assets|flags|`FORCE_INSERT`,`FORCE_CONTINUE`|


|flag|description|
|----|------------|
|`FORCE_INSERT`|Mostly relevant to developers. Will force the insert of a row even if it contains an asset_id. This is useful to import an export from another system while retaining the asset_id values.|
|`FORCE_CONTINUE`|Instead of failing on the first error the import will continue instead writing all failing rows to an exception csv file|

### File
The file contains the URLs to the Core and Auth services, as well as the username and password 
required to log onto the system

E.g.
```
{
  "authUrl" : "http://localhost/auth2/login",
  "serviceUrl": "http://localhost:8669",
  "dbUsername" : "dev",
  "dbPassword": "dev"
}
```
 
Exception files
---------------- 
 
 An exception file is generated for each type of asset being imported. The file is in exactly 
 the same format as the imports allowing you to make updates in the file and use it as a source to import from. 
