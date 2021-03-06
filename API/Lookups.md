API
====

The Lookups API, provides a simple mechanism to query data.  It only supports very basic functionality:

1. Retrieving rows from a view or table based on basic set of query parameters. *All columns are returned.*
2. Store and Retrieve to/from KV tables via the **code** assigned to that kookup table


### API Versioning

The draft [API versioning] (https://imqssoftware.atlassian.net/wiki/x/eoCZZw) system is has not yet been implemented.

### Rules

[Rules and conventions ](https://imqssoftware.atlassian.net/wiki/x/S4CBeQ)

## Security

See [API Security](APISecurity.md) for an overview of how security is implemented and the requirements of making secured REST calls.
The current implementation of this API: 
* *Currently* only supports token based authentication, **not** inter-service authentication
* *Currently* only enforces authentication and **not** authorisation

## Status codes

|Code|Meaning|Explanation|
|----|-------|-----------|
|200|OK|Operation completed successfully|
|201|Created|Entity was created successfully|
|400|Bad request|The Operation requested is improperly formatted - typically a validation failure.
|403|Forbidden|User does not have permission to perform the action|
|404|Not found|Entity not found. Only used in cases where an empty collection cannot be used as return value|
|408|Request timeout|The client should resubmit the request|
|409|Conflict|The entity that you tried to add already exists|
|412|Precondition failed|Indicates that the requested Operation would violate a business rule|


## KV Interface ##

#### Lookup type
|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|`code`|`string`|m||
|`name`|`string`|m||
|`description`|`string`|o||
|`owner`|`string`|o||
|`table`|`string`|o|Fully qualified table name `<schema>.<tablename/viewname>`|

#### Lookup value
|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|`k`|``|m||
|`v`|``|m||
|`creation_date`|``|o||
|`activated_at`|``|o||
|`deactivated_at`|``|o||
|`allow_delete`|``|o||
|`geom`|``|o||


### `GET /lookups/kv/{code}`
Returns all of the K/V values in the specified KV table

Accepts: *Nothing*

Returns: `[Lookup value]`
Status codes: 200, 400, 403, 404, 408


### `GET /lookups/v/{code}/{k}`
Returns the lookup value from the kv table with the specified code, using the specified key (`k`).

Accepts: *Nothing*

Returns: `String`

Status codes: 200, 400, 403, 408


### `GET /lookups/kv/{code}/{k}`
Returns the lookup row from the kv table with the specified code, using the specified key (`k`).

Accepts: *Nothing*

Returns: `Lookup value`

Status codes: 200, 400, 403, 408



### `PUT lookups/kv` (NOT IMPLEMENTED)
Registers a new kv lookup table.

Accepts: 
```
{
  "name" : "...",
  "description" : "...",
  "owner" : "...",
  "table" : "..."
}
```
Returns: *Nothing* 

Status codes: 201, 400, 403, 408, 409, 412


### `POST /lookups/kv/{code}`
Upserts rows to the kv table with the specified code.

Accepts: 
```
[
  {
    "k" : "...",
    "v" : "...",
    "description" : "..."
  }
]
```
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `DELETE /lookups/kv/{code}/{k}` (NOT IMPLEMENTED)
Delete the lookup value with the key (`k`) from the kv table with the specified code.


Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `POST /lookups/kv/{code}?k="..."&v="..."&description="..."` (NOT IMPLEMENTED)
Upserts a lookup value to the kv table with the specified code.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `DELETE /lookups/kv/{code}/keys` (NOT IMPLEMENTED)
Deletes the lookup values that have the specified key from the kv table with the specified code.

Accepts: 
```
[
  String
]
```
Returns: *Nothing*

Status codes: 200, 400, 403, 408, 412

## Generic table/view lookup interface


>**This interface operates on the raw database names and as such the scehma name must be included in the name of the source being read from. The period in the name should be replaces with `+`.**


### `GET /lookups/{source}` 
Returns all columns from the table/view (`source`)

Accepts: 

A map of `field:value`. Each such pair forms a filter criterion. The criteria are implicitly **AND**-ed.

```
{
  column name 1 : "..."
  column name 2 : "..."
  ...
  column name n : "..."
}
```

Returns: 

A list of objects, with each object containing a field and value for each column in the underlying row.

```
[
  {
    ...
  }
]
```

Status codes: 200, 400, 403, 408

### `GET /lookups/{source}/using_operators?f1={"operator":"...",value:"..."}&f2={"operator":'...',"value":"..."}&...` 
Returns all columns from the table/view (`source`)

Accepts: 

A map of `field:{"operator" : "...", "value" : ".."}` specified as URL parameters. Each such entity forms a filter criterion. The criteria are implicitly **AND**-ed.

The operator may be one of `<`,`>`,`=`,`!=`.


> E.g. `http://localhost/lookups/dtpw+ref_client_department/using_operators?chief_directorate_code={"operator":"=","value":"CD_GI"}`
>
> Retrieves the contents of the `dtpw.ref_client_department` table where the field `chief_directorate_code` is `=` to `CD_GI`.

Returns: 

A list of objects, with each object containing a field and value for each column in the underlying row.

```
[
  {
    ...
  }
]
```

Status codes: 200, 400, 403, 408