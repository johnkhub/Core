HOW TO
======

# Adding an Asset sub-type

 1. Update schema (Asset) to add the new table. Remember to add foreign keys and indexes.
 2. Add the name of the new type into `asset_type`
 3. Add a `DTO`. Stick to the naming convention
 4. Add the appropriate OpenCsv annotations to this class
 5. Ass the new class to `@JsonSubTypes` in `CoreAssetDto`
 6. Add a new domain model class. Again stick to the naming convention.
 7. Update the `T createAsset(D dto)` method in `AssetFactory`
 8. The final changes are in `CoreAssetWriterImpl`. Unless you need to execute something other 
 than simple update/insert statements using simple data types you should not need to make any 
 changes here.

# Adding a Kv sub-type

> Similar to the above

1. Add KV table by copying other table in json schema
2. Add entry in `kv_type` table
3. Add check constraint in k column inside the appropriate `checkconstraint.sql` file
4. ~~Add KV type to importer code~~

# Adding a schema

To add another schema look at at [this](src/main/resources/README.md).



