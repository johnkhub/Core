Operations
==========

TBD

Operations
- Acquisitions adds assets (PCS, Acquisitions module(in Outsystems))
- Subdivision & Consolidation of assets  (Land Management module)
- Movement of assets in the hierarchy




API
---

### API Versioning

The draft [API versioning] (https://imqssoftware.atlassian.net/wiki/x/eoCZZw) system is has not yet been implemented.

### Rules

[Rules and conventions ](https://imqssoftware.atlassian.net/wiki/x/S4CBeQ)

### Security

See [API Security](APISecurity.md) for an overview of how security is implemented and the requirements of making secured REST calls.

The current implementation of this API: 
* *Currently* only supports token based authentication, **not** inter-service authentication
* *Currently* only enforces authentication and **not** authorisation

### Status codes

|Code|Meaning            |Explanation                                                                     |
|----|-------------------|--------------------------------------------------------------------------------|
|200 |OK                 |Operation completed successfully                                                |
|201 |Created            |Entity was created successfully                                                 | 
|400 |Bad request        |The Operation requested is improperly formatted - typically a validation failure|
|403 |Forbidden          |User does not have permission to perform the action                             |
|404 |Not found          |Entity not found.   Where possible an empty resultset e.g. `[]` is returned rather than this code |
|408 |Request timeout    |The client should resubmit the request                                          |
|409 |Conflict           |The entity that you tried to add already exists                                 |
|412 |Precondition failed|Indicates that the requested Operation would violate a business rule            |
|413 |Payload too large  |Indicates that the server truncated the resultset                               |

### Objects

#### Core Asset Dto

|Field  |Type  |o/m  |Description|
|-----------------------|--------|----|-----------|
|`asset_type_code`      |`string` |m  |One of 'ENVELOPE', 'FACILITY', 'BUILDING', 'SITE','FLOOR', 'ROOM', COMPONENT, LANDPARCEL etc. Mandatory on create.|
|`code`                 |`string` |(m)|Mandatory on create.|
|`name`                 |`string` |(m)|Mandatory on create. Optional on update|
|`adm_path`             |`string` |o  ||
|`func_loc_path`        |`string` |(m)|Mandatory on create.|
|`creation_date`        |`string` |o  ||
|`address`              |`string` |o  ||
|`geom`                 |`string` |o  ||
|`latitude`             |`number` |o  ||
|`longitude`            |`number` |o  ||
|`barcode`              |`string` |o  ||
|`serial_number`        |`string` |o  ||
|`region_code`          |`string` |o  ||
|`district_code`        |`string` |o  ||
|`municipality_code`    |`string` |o  ||
|`town_code`            |`string` |o  ||
|`suburb_code`          |`string` |o  ||
|`ward`                 |`string` |o  ||
|`responsible_dept_code`|`boolean`|o  |**DTPW SPECIFIC**|
|`is_owned`             |`boolean`|o  |**DTPW SPECIFIC**|

> Depending on the type of Asset (as defined by `asset_type_code`) more fields specific to this type of Asset may be added to the entity.


#### Quantity Dto

|Field                  |Type     |o/m |Description|
|-----------------------|---------|----|-----------|
|`asset_id`             |`uuid`   |m   |UUID of the asset to which this quantity pertains|
|`unit_code`            |`string` |(m) |Mandatory on create.|
|`name`                 |`string` |(m) |Mandatory on create.|
|`num_units`            |`string` |o   |Decimal value transported in string|



### `PUT assets/{uuid}`
Creates a new Asset.

Accepts:  `CoreAssetDto`
```
e.g.
{
  "asset_type_code" : "ENVELOPE"
  "code" : "..."
  "name" : "..." 
  "adm_path" : "..."
  "func_loc_path" : "..."
  "creation_date" : "..."
  "address" : "..."
  "geom" : "..."
  "latitude" : "..."
  "longitude" : "..."

  "barcode" : "..."
  "serial_number" : "..."
  ...
}
```

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `PATCH assets/{uuid}`
Updates an existing Asset.

> **RULE:** Changing the Asset Type is not allowed.

Accepts:  `CoreAssetDto`
```
e.g.
{
  "asset_type_code" : "ENVELOPE"
  "code" : "..."
  "name" : "..." 
  "adm_path" : "..."
  "func_loc_path" : "..."
  "creation_date" : "..."
  "address" : "..."
  "geom" : "..."
  "latitude" : "..."
  "longitude" : "..."

  "barcode" : "..."
  "serial_number" : "..."

  ...
}
```

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 412

### `DELETE assets/{uuid}?permanent={permanent}`
Deletes an Asset.  If `permanent` is `true` then the Asset is removed from the database, else it is marked as inactive.
> Permanent deletion is only possible if the Core service is run in with the `admin` profile. See [CONFIG.md](../Service/CONFIG.md)

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 409, 412



### `GET assets/link/types`
Returns a list of defined external identifier types

Accepts: *Nothing*

Returns:

```
[
  {
    "uuid": "..."
    "description": "..."
  }
]
```

Status codes: 200

### `PUT  assets/link/{uuid}/to/{external_id_type}/{external_id}`

Links an external identifier of the specified type to the specified Asset. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 412

### `DELETE assets/link/{uuid}/to/{external_id_type}/{external_id}`

Removes the specified external identifier of the specified type linked to the specified Asset. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 412

### GET `assets/link/{uuid}/to/{external_id_type}`

Returns the link to the specified asset of the given external type. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: 
```
   <external_id>
```

Status codes: 200, 400, 403, 404,  408






> QUERY API: under construction

### GET `assets/{uuid}` 

Returns the Asset with the given asset_id.

Accepts: *Nothing*

Returns: `CoreAssetDto`

Status codes: 200, 400, 403, 404, 408



### GET `assets/func_loc_path/{path}`

Returns the Asset with the given Functional Location Path.
> **NOTE**: The path must use `+` instead of `.` as a segment delimeter, when it is specified in the URL

Accepts: *Nothing*

Returns: `CoreAssetDto`

Status codes: 200, 400, 403, 404, 408


### GET `assets/linked_to/{external_id_type}/{external_id}`

Returns the Asset with the given External Identifier. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: `CoreAssetDto`

Status codes: 200, 400, 403, 404, 408


### GET `assets/query?filter`

Accepts a complex filter request and returns the Assets that match.

Accepts: *Nothing*

Returns: `[CoreAssetDto]`

Status codes: 200, 400, 403, 404, 408, 413

#### Parameters

|Parameter|Type|Description                                                          |
|---------|--------|-----------------------------------------------------------------|
|filter   |`String`|See below                                                        |
|~~groupby~~|~~`String`~~|~~Comma separated list of field names. Translates to equivalent SQL~~|
|orderby  |`String`|Comma separated list of field names. Translates to equivalent SQL|
|offset   |`long`  |Facilitates paging                                               |
|limit    |`long`  |Facilitates paging. Note that even if no limit is supplied the server may truncate the resultset if it is too large|

> Orderby supports the `asc`|`desc` suffix

#### Filter syntax

Filters allow you to specify sets of relational expressions joined by AND or OR. Grouping may be achieved using round brackets (`'('` `')'`).

The filter mechanism is simple by design and somewhat restrictive.  The goal here is to have a very thin abstraction of the underlying SQL database implementation. Mostly to guard against SQL
injection attacks, but also to maintain portability between the REST API and client applications.

>**CAVEAT**
> * The abstraction layer does as little as possible and this results in many of the errors in filters only manifesting themselves as SQL excution errors.
> * The reporting of parsing errors in the initial implementation is staggeringly bad. Sorry.


|Operator|Applies to                             |Description                                        |
|--------|---------------------------------------|---------------------------------------------------|
|=       |String, Number, Boolean, DateTime, Path|Refer to `LOWER` built-in function below           |
|!=      |String, Number, Boolean, DateTime, Path|                                                   |
|<       |Number, DateTime, Path                 |For a Path, gets the children of the supplied path |
|<=      |Number, DateTime                       |                                                   |
|>       |Number, DateTime, Path                 |For a Path, gets the ancestors of the supplied path|
|>=      |Number, DateTime                       |                                                   |
|~~LIKE~~    |~~String~~                                 |~~Mapped to SQL `LIKE`~~                               |

* `String`, `Number` and `Boolean` are the Core Asset DTO fields .
* `DateTime` is a Timestamp value transported as a `String` in the DTO.
* `Path` is a `String` enclosed in `@()` e.g. `func_loc_path > @('1718+1718+B009_B1')`

Relational expressions have the form `<fieldname> <operator> <literal value>`
The expression may be prefixed by `NOT`.

> CAVEAT 1: Boolean expressions must include the = true/false e.g. `... and is_owned` is invalid  whereas `... and is_owned=true` is valid.

> CAVEAT 2: Boolean fields that are null in the database are treated `false` for the purposes of comparision and are returned as `false` to avoid complicating things for the calling party.

> CAVEAT 3: The path must use `+` instead of `.` as a segment delimeter, when it is specified in the URL

#### Built-in functions

* `LOWER`:  The lower function can be applied to a text field. This is to facilitate case  insensitive comparison. e.g. `... and LOWER(name) = 'groote schuur'`
> Note that the function cannot be applied to a string literal and that the literal must be all lower case when supplied in the filter for this to work as intended.

#### Tags ####

The `TAGS` pseudo function provides a means to filter for Assets having a specifci combination of Tags.

e.g.
```
 ...and TAGS['AT_RISK','BLUE']...
```

or

```
 ...and NOT TAGS['AT_RISK','BLUE']...
```

The matching of the set of Tags is exact.  If you want to test for any one of a combination of Tags, use multiple `TAGS` expressions joined by `or` e.g.
```
...and (TAGS['AT_RISK'] or TAGS['BLUE'])...
```
> **CAVEAT**: You can apply this function to any text field, however effective excution requires that the field be indexed on lowercase as well.  The `address` and `name` are the only columns that currently have such indexes. 

#### Available fields ####

|All                        |Envelope           |Facility           |Site|Building|Floor|Room|Landparcel|
|---------------------------|-------------------|-------------------|----|--------|-----|----|----------|
|asset_id	                  |                   |facility_type_code	|    |        |     |    | LPI      |
|asset_type_code            |                   |    	              |    |        |     |    |          |
|code                       |                   |    	              |    |        |     |    |          |
|name                       |                   |    	              |    |        |     |    |          |
|func_loc_path              |                   |    	              |    |        |     |    |          |
|address                    |                   |    	              |    |        |     |    |          |
|latitude                   |                   |    	              |    |        |     |    |          |
|longitude                  |                   |    	              |    |        |     |    |          |
|geom	                      |                   |    	              |    |        |     |    |          |
|creation_date              |                   |                   |    |        |     |    |          | 
|deactivated_at             |                   |                   |    |        |     |    |          | 
|barcode                    |                   |                   |    |        |     |    |          | 
|serial_number              |                   |                   |    |        |     |    |          | 
|region_code                |                   |                   |    |        |     |    |          |
|district_code              |                   |                   |    |        |     |    |          |
|municipality_code          |                   |                   |    |        |     |    |          |
|town_code                  |                   |                   |    |        |     |    |          |
|suburb_code                |                   |                   |    |        |     |    |          |
|ward                       |                   |                   |    |        |     |    |          |
|responsible_dept_code (*)  |                   |    	              |    |        |     |    |          |
|is_owned (*)               |                   |    	              |    |        |     |    |          |
> (*) are **DTPW specific**


For **DTPW**, certain fields are specific to a specific Chief Directorates.  The presence of the field is dictated by the Chief Directorate.


|CD|All             |Envelope           |Facility           |Site|Building|Floor|Room|Landparcel|
|--|----------------|-------------------|-------------------|----|--------|-----|----|----------|
|EI|EMIS    	      |                   |                   |    |        |     |    |          |
|EI|ei_district_code|                   |                   |    |        |     |    |          |



> **NOTE:** These fields/filter criteria are not available
> * Branch, Chief Directorate (planned)
> * The link between Landparcels and Assets
> * Any form of spatial criteria (*not planned*)

#### Examples ####


`http://localhost:8669/assets/query?filter=%22asset_id=0000020a-7b8b-4a35-89f1-4f8d8349855e and name = %27building1%27 %22&orderby=%22func_loc_path,assetid%22%3Fasc&offset=50&limit=50`

### GET `assets/landparcel/{uuid}/assets`

Returns the Asset identifiers linked to the Landparcel with the given id.

Accepts: *Nothing*

Returns: `[String]`

Status codes: 200, 400, 403, 404, 408


### PUT `assets/landparcel/{landparcel_id}/asset/{asset_id}`

Links the specified asset id to the specified Landparcel id.

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 404, 408



### PUT `/group/{uuid}/to/{grouping_id_type}/{grouping_id}`

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 404, 408


### PATCH `assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}`

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 404, 408


### DELETE `assets/group/{uuid}/to/{grouping_id_type}/{grouping_id}`

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 408


### GET `assets/group/{uuid}/to/{grouping_id_type}`

Accepts: *Nothing*

Returns: `[String]`

Status codes: 200, 400, 403, 404, 408


### GET `assets/group/types`

Returns a list of defined grouping identifier types

Accepts: *Nothing*

Returns:

```
[
  {
    "uuid": "..."
    "name": "..."
    "description" : "..."
  }
]
```

Status codes: 200, 400, 403, 408


### GET `assets/grouped_by/{grouping_id_type}/{grouping_id}`

Returns the Assets sharing the  grouping Identifier. Use `GET asset/group/types` to list the grouping identifier types that exist on your system.

Accepts: *Nothing*

Returns: `[CoreAssetDto]`

Status codes: 200, 400, 403, 408
    



### PUT `/table/{table}/field/{field}/asset/{uuid}/value/{value}`

Populate a specific field in a specific table. **Note:** The table name must be qualified with the name of the schema replacing the `.` with a `+`.
E.g. `dtpw+ei_district_link`

> * This is a low-level API that is likely to change or fall away
> * Only xxx_link tables in the client specific schems are accessible



Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 408



### PATCH `/table/{table}/field/{field}/asset/{uuid}/value/{value}`

Populate a specific field in a specific table. **Note:** The table name must be qualified with the name of the schema replacing the `.` with a `+`.
E.g. `dtpw+ei_district_link`

> * This is a low-level API that is likely to change or fall away
> * Only xxx_link tables in the client specific schems are accessible



Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 404, 408


### DELETE `/table/{table}/field/{field}/asset/{uuid}`

Clears the value of a specific field in a specific table. **Note:** The table name must be qualified with the name of the schema replacing the `.` with a `+`.
E.g. `dtpw+ei_district_link`

> * This is a low-level API that is likely to change or fall away
> * Only xxx_link tables in the client specific schems are accessible

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 200, 400, 403, 408



### PUT `/assets/quantity`

Inserts the specified Quatity into the database. 

Accepts: `QuantityDto`

Returns:  *Nothing*

Status codes: 201, 400, 403, 408, 409

### PATCH `/assets/quantity`

Updates the specified Quantity in the database.
> The combination of `asset_id` and `name` must resolve to an existig entry in the database.

Accepts: `QuantityDto`

Returns:  *Nothing*

Status codes: 201, 400, 403, 408, 409


### DELETE `/assets/quantity/asset_id/{asset_id}/name/{name}`

Deletes the Quantity matching the specified `asset_id` and `name` from the database.

Accepts: *Nothing*

Returns:  *Nothing*

Status codes: 201, 400, 403, 404, 408


### GET `/assets/quantity/asset_id/{asset_id}/name/{name}`

Retrieves the Quantity matching the specified `asset_id` and `name` from the database.

Accepts: *Nothing*

Returns:  `QuantityDto`

Status codes: 201, 400, 403, 404, 408
