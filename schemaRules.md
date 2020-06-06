Use of data types
=================

* Code fields and Keys (`k`) in Lookup Tables are `VARCHAR(10)`
* Money is `DECIMAL(19,4)`
* Lat/Lon is `DECIMAL(11,8)`

Naming rules
============
Take note of the identifier length limit of *63* imposed by Postgres. Try and keep table and field names short in case they need to be combined to form names in keys and indexes.

Comments
--------
* Many if not most entities in the various schemas have comments i.e. `COMMENT ON XXX IS...` Please add comments as you go along.

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

**Note**: `CREATE TABLE / UNIQUE` will create an implicit index `example_a_b_key` for table `example`

(See [Stackoverflow](https://stackoverflow.com/questions/4107915/postgresql-default-constraint-names/4108266#4108266))
