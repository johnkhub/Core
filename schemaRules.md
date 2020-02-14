Naming rules
============
Take not of the identifier length limit of *63* imposed by Postgres. Try and keep table and field names short in case they need to be 
combined to form names in keys and indexes.

Basic
----
We primarily target Postgres so it makes sense to adopt its conventions.

* SQL keywords `UPPERCASE`
* identifier names `lower_case_with_underscores`


Lookup tables
-----------------------
* Are named `ref_xxxx`
* Inherit from `kv_base`

Naming conventions
------------------
* Primary Keys TBD
* Foreign Keys TBD
* Linking Tables TBD
* Indexes TBD
* Sequences TBD
* Function names
* Stored procedure names

The standard names for indexes in PostgreSQL are:

{tablename}_{columnname(s)}_{suffix}

where the suffix is one of the following:

*pkey* for a Primary Key constraint
*key* for a Unique constraint
*excl* for an Exclusion constraint
*idx* for any other kind of index
*fkey* for a Foreign key
*check* for a Check constraint
*seq* for all sequences

**Note**: `CREATE TABLE / UNIQUE` will create implicit index `example_a_b_key` for table `example`

(See [Stackoverflow](https://stackoverflow.com/questions/4107915/postgresql-default-constraint-names/4108266#4108266))
