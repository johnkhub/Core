NOTES
======

# Adding an Asset sub-type

 1. Update schema (Asset) to add the new table. Remember to add foreign keys and indexes.
 2. Add the name of the new type into `asset_type`
 3. Add a `DTO` stick to the naming convention
 4. Add the appropriate OpenCsv annotations to this class
 5. Ass the new class to `@JsonSubTypes` in `CoreAssetDto`
 6. Add a new domain model class. Again stick to the naming convention.
 7. Update the `T createAsset(D dto)` method in `AssetFactory`
 8. The final changes are in `CoreAssetWriterImpl`. Unless you need to execute something other 
 than simple update/insert statements using simple data types you should not need to make any 
 changes here.
 
 # Imports
 
 1. If asset_id column set, then we do an update?
 
 
 # Known problem that came out 
 
 * After creating but before restoring the db, we need to install the extensions manually?
 * There were asset entries in both envelope and facility for the same asset - how can we impose a db constraint avoid this
 * We need a delete function to clear out all the various tables.
     * One for the core tables
     * Other ones for the extensions?
 * Must now ensure validation on the paths in the core all codes in the path need to exist
     * Can probably implement a function to validate the path as part of a trigger?
     * This might imply that we need to insert entries in path order so parent is always created before child - implies sorting 
 * Need a mechanism to find garbage in the database
     * Assets linked to multiple sub-types as mentioned above
     * Assets with invalid paths as mentioned above
     
 * The DTPW view does not  seem to show everything it should - maybe when there are holes in the path? Definitely an issue with Land Parcels
 * Especially the LPI import had lost of issue with whitespace around values and space/empty strings instead of blanks
 * Responsible dept made nullable
 * Length LPI column now 26 and check constraint to check exactly 21 characters was removed 
