HOW TO
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

# Adding a Kv sub-type

 > Similar to the above
