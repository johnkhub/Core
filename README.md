# Core Schema

Target Audience
---------------
|Section|Audience|
|---|---|
|0 Introduction|All|
|1 Core Directory|All|
|2 Transactions|At this stage probably only of interest to people with an Asset management hat on, unless you are interested in maintaining history|
|3 Life Cycle & Financials|At this stage probably only of interest to people with an Asset management hat on|
|4 Shared Data|All as it is relevant to all modules - it provides shared models|
|5 Audit|Take not for now. This will be accessed by as yet undefined APIs and can be retrofitted|
|6 Lookups|All as it is relevant to all modules|
|7 Hierarchies|All as it provides the paths taht need to go into the Core Directory, but the section is too incomplete to waste time on now|


House Rules
------------

* See [Schema Rules](schemaRules.md) for the naming conventions we employ.
* When making changes to the schema, the `.io` file is considered the master copy, so load this into dbdocs (or edit in a text editor) and then be sure to save it to both dbdocs and update the text file and commit to GitHub.

0 Introduction 
==============

Data is captured at a very granular level and aggregated to render various views on the data. These views are what end up in the various front-ends.

Ideas
-----
* Rather than building a fully dynamic system, add columns or tables and adjust the aggregations accordingly.

Principles
-----------
 1. Use `UUID`s so we can copy data between databases
 2. Use path strings: instead of having to traverse the trees do `like` queries
 3. Consider adding addition tags to this table for grouping
 4. We don't delete

Data Types
----------------
 * Codes are `VARCHAR(10)` - what did we use in Classification service?
 * Money is  `DECIMAL(19,4)`
 
About paths
-----------
```
 NOTE USING PATH STRINGS ALSO MAKES IT POSSIBLE TO EASILY HANDLE CHANGES OF COMPONENTS - USING EXPLICIT PARENT CHILD RELATIONSHIPS MAKE THIS IMPOSSIBLE


     [a]
    /   \
   [b] [c]
  /
 [d]

 [e] now replaces [b] and we don't delete [b] according to our basic  principles. This leaves [d] with two parents.


      [a]
    /  \  \
  [b] [e]  [c]
  /   /
   [d]

  We can mark [b] as inactive
```

1 Core Directory
================

To be modeled in the system an Asset has to reside in at least Asset. Optionally it may have:
* A Location e.g. Lat/Long and a path in the Location Hierarchy
* Spatial information
* Alias identities as defined by external systems (where we integrate)
* An Asset Data Model

https://dbdiagram.io/d/5da990c102e6e93440f25ddb

The Asset Table contains the primary UUID of the Asset a number of path strings (ad a column to add a path)


Core API
--------
* All insertions and updates must go through the API
* Must 
  + Authenticate
  + Audit log



2 Transactions
================

https://dbdiagram.io/d/5def6931edf08a25543ee9b6

Attribute updates
-----------------
TBD

Financial Transactions
----------------------

Transactions decouple the submitted changes from the actual database representation of the entities that they affect. For now the plan is to fork the updates to the Transactions tables and the current state of the Assets. We therefore have all history and the exact current state. This will be very performant for reading current financial values, but opening and closing values will require traversing the Transaction table.

If you want the state at a different point in history, you roll up all transactions from a given point in history to the required date. This gets rendered to a different set of tables that we can name and persist under the name of the user and the date (say).

To speed up the rendering, we may choose to keep some snapshots at say the end of a financial period. We must remember to trash these when we apply a corrective historic transaction though.


### Changing an Asset 

One transaction corresponds to a single field change brought about by the specific type of transaction executed. If multiple fields are affected by the type of transaction you will have multiple rows in the Transaction table. They are linked by a BatchId.

### Open questions

* The transaction mechanism will be able to track all Changes, including changes to the structure of the trees. Do we want to track all of them though?


Performance
-----------
Needles to say the Transaction tabel will rapidly grow in size. We shoul make use of the Postgres table partition functionality

e.g. if we partition by Year:

```
CREATE TABLE Transaction (
 ...

   EffectiveDate ...
 ...  

) PARTITION BY RANGE (EffectiveDate);
```
Add a partition on each financial year roll-over
```
CREATE TABLE Transaction_2019 PARTITION OF Transaction

   FOR VALUES FROM ('2019-01-01') TO ('2019-12-31');

CREATE TABLE Table_2020 PARTITION OF Transaction

   FOR VALUES FROM ('2020-01-01') TO ('2020-01-01');
```

3 Life Cycle & Financials
=========================

These link `1:{0,1}` to Assets

https://dbdiagram.io/d/5def6a03edf08a25543ee9c4


**Observation:** *Lifecycle and financials do not require tamper check if they can be regenerated*

Financials
----------

Financial Transactions known so far:
  * DATALOAD (The setting of the values when we create the asset register)
  * ADDITION
  * WIP TRANSFER
  * DEPRECIATE
  * UPGRADE
  * IMPAIR
  * REVALUE
  * MAINTENANCE
  * RECLASSIFY
  * DERECOGNISE


For a given Asset the Financial Values can be calculate from the Transaction table as listed below. Note the following place holders:

|Placeholder|Description|
|---|---|
|`$FIN_YEAR_START$`|The start of the Financial Year (year/month/day)|
|`$FIN_YEAR_END$`|The end of the Financial Year(year/month/day)|
|`$NOW$`|The current timestamp|

Also **THERE IS OF COURSE A WHERE AssetId = xxxx REQUIRED, I have omitted it to keep things concise.**


|Account|Derived value|As|
|:---|:---|:---|
|Depreciation|DepreciationLastDate|`select MAX("EffectiveDate") as "DepreciationLastDate" from |Transaction where "TransactionType" = 'DEPRECIATION' and "EffectiveDate" < $FIN_YEAR_END$`|
||DepreciationOpening|`select SUM("Amount") as "DepreciationClosing" from Transaction where TransactionType = 'DEPRECIATION' and Field = 'Depreciation' and "EffectiveDate" <= $FIN_YEAR_START$`|
||DeprecitiationClosing|`select SUM("Amount") as "DepreciationClosing" from Transaction where TransactionType = 'DEPRECIATION' and Field = 'Depreciation' and "EffectiveDate" < $FIN_YEAR_END$`|
|| DepreciationFinYtd|`select SUM("Amount") as "DepreciationFinYtd"  from Transaction where TransactionType = 'DEPRECIATION' and Field = 'Depreciation' and "EffectiveDate" < $NOW$`|
|Impairment|||
|Cost|CostOpening|`select SUM("Amount") as "CostOpening" from Transaction where Field = 'Cost' and "EffectiveDate" <= $FIN_YEAR_START$`|
||CostClosing|`select SUM("Amount") as "CostClosing" from Transaction where Field = 'Cost' and "EffectiveDate" < $FIN_YEAR_END$`|
||TransferCost|`select SUM("Amount") as "TransferCost"  from Transaction where Field = 'Cost' and "EffectiveDate" < $NOW$`|
|Reclassification|ReclassificationLastDate|`select MAX("EffectiveDate") as "ReclassificationLastDate" from Transaction where "TransactionType" = 'RECLASSIFICATION' and "EffectiveDate" < $FIN_YEAR_END$`|
||ReclassificationCost|`select SUM("Amount") as "ReclassificationCost" from Transaction where TransactionType = 'RECLASSIFICATION' and Field = 'Cost' and "EffectiveDate" < $NOW$`|
||ReclassificationDepreciation|`select SUM("Amount") as "ReclassificationDepreciation"  from Transaction where TransactionType = 'RECLASSIFICATION' and Field = 'Depreciation' and "EffectiveDate" < $NOW$`|
||ReclassificationImpairment|`select SUM("Amount") as "ReclassificationImpairment"  from Transaction where TransactionType = 'RECLASSIFICATION' and Field = 'Impairment' and "EffectiveDate" < $NOW$`|
|Revaluation|RevaluationLastDate|`select MAX("EffectiveDate") as "RevaluationLastDate" from Transaction where "TransactionType" = 'REVALUATION' and "EffectiveDate" < $FIN_YEAR_END$`|
||RevaluationReserveOpening|``|
||RevaluationReserveClosing|``|
||RevaluationReserveFinYTD|``|
||RevaluationReserveFinYTDDepreciation|``|
||RevaluationReserveFinYTDImpairment|``|
||RevaluationAmount||`select SUM("Amount") as "ReclassificationImpairment"  from Transaction where TransactionType = 'REVALUATION' and Field = 'Cost' and "EffectiveDate" < $NOW$`|
|Derecognition|DerecognitionDate|`select MAX("EffectiveDate") as "DerecognitionDate" from Transaction where "TransactionType" = 'DERECOGNITION' and "EffectiveDate" < $FIN_YEAR_END$`|
||DerecognitionCost|``|
||DerecognitionDepreciation|``|
||DerecognitionImpairment|``|		
||RevaluationReserveFinYTDImpairment|``|
||RevaluationReserveOpening|``|
||RevaluationAmount|``|
|Addition|AdditionLastDate|`select MAX("EffectiveDate") as "AdditionLastDate" from Transaction where "TransactionType" = 'ADDITION' and Field = 'Cost' and "EffectiveDate" < $FIN_YEAR_END$`|
||AdditionOpening|`select SUM("Amount") as "AdditionOpening" from Transaction where TransactionType = 'ADDITION' and Field = 'Cost' and "EffectiveDate" <= $FIN_YEAR_START$`|
||AdditionClosing|`select SUM("Amount") as "AdditionClosing" from Transaction where TransactionType = 'ADDITION' and Field = 'Cost' and "EffectiveDate" < $FIN_YEAR_END$`|
||AdditionFinYTD|`select SUM("Amount") as "AdditionFinYtd"  from Transaction where TransactionType = 'ADDITION' and Field = 'Cost' and "EffectiveDate" < $NOW$`|				
					
					
These values are calculated from other calculated values:
* CarryingValue		
* CarryingValueOpening							
* CarryingValueClosing


Life Cycle
-----------

All updates other than to financial value are set replacing a ond value with a new value. You could go for a dynamic solution 
based on something like:

```
select 
	(
	  case
		  when "DataType" = T_SLONG then Delta_T_SLONG
		  when "DataType" = T_ULONG then Delta_T_ULONG
		  when "DataType" = T_MONEY then Delta_T_MONEY
		  when "DataType" = T_STRING then Delta_T_STRING
		  when "DataType" = T_BOOLEAN then Delta_T_BOOLEAN
		  when "DataType" = T_POLYGON then Delta_T_POLYGON
		  else
	  end
  	) as "Value"
  from from Transaction where EffectiveDate < $NOW$ order by EffectiveDate desc limit 1`
```

But, since you know what the types are for the individual fields, you can construct a view that directly queries the correct field

```
  select Delta_T_ULONG as "aaa" from Transaction where EffectiveDate < $NOW$ order by EffectiveDate desc limit 1`
		
```



4 Shared Data
=================

These are some general concepts that will prove useful to many modules. They are logically part of the Core,
but one would expect them to be loosely coupled to Assets (i.e. the majority of Assets won't have any)

* Supplier
* Facility via AssetFacility linking table
* Unit (as in SI or Imperial)


https://dbdiagram.io/d/5def646cedf08a25543ee970



5 Audit
=========

Actions against Assets generate entries in the Audit Log via the linking table AuditLink

https://dbdiagram.io/d/5def5fd8edf08a25543ee91e

There is an obvious duplication of data between the explicit Audit Log and the history provided by the Transaction table.  I would at this stage rather live with this duplication
rather than having everything audit related be underpinned by something that we have not completely figured out yet.

**Initially we would need to make the field nullable** as we won't be writing the audit trail.

6 Lookups
=========

These are intended to also be shared between all of the modules for read purposes. Only the `Owner` (see schema) is allowed to write to the table.  We will have to figure out how to manage the identities of the owners and how to manage this.

https://dbdiagram.io/d/5def616cedf08a25543ee93f

The front-end already supports reading from db.table.field. We should embrace this and add a schema specifier.

7 Hierarchies
=============


These are managed in the Classifications and Templates databases. The hierarchies themselves are maintained in the Classifications database, whereas the attribute data that constitute the model parameters reside in Template service.  IMQS Keys manages the data in these two services. Other services exchange path strings only.


//

//
// Template Database (TBD)
// 
Table AssetADM {
  AdmId UUID  PK
  DescriptorSizeUnit   VARCHAR(10) [not null]
  ComponentDesc        VARCHAR
  Extent               DECIMAL(19,4) [null]  
  ExtentUnit           VARCHAR(10) [not null]
  ExtentUnitRate       DECIMAL(19,4) [null]  
  MeasurementModelId   VARCHAR(10) [not null, note: 'These may be enums?']
  DepreciationMethodId VARCHAR(10) [not null, note: 'These may be enums?']
  
  TamperCheck VARCHAR [not null]
}

Ref: "AssetADM"."DescriptorSizeUnit" - "Unit"."Code"
Ref: "AssetADM"."ExtentUnit" - "Unit"."Code"

https://dbdiagram.io/d/5def616cedf08a25543ee93f

### Location Hierarchy (TBD)
```
   RegionCode
     WardCode
       SuburbCode
         Street
           Stand
             Building
               Floor
                 Room
```



### ADM Hierarchy
TBD

### Functional Location
TBD

### Financial Hierarchy
TBD

### SCOA Hierarchies
TBD

8 Getting going
===============
 1. Create a new postgres database called Core.
 2. Run `populate.sql` to create the tables and load basic data
 
 **Note:** You may want to skip loading the dummy data - there are a very large number of wards for instance
