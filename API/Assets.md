Operations
==========


Operations
- Acquisitions adds assets (PCS, Acquisitions module(in Outsystems))
- Subdivision & Consolidation of assets  (Land Management module)
- Movement of assets in the hierarchy

Rules
----


API
---

### Status codes

|Code|Meaning|Explanation|
|----|-------|-----------|
|200|OK|Operation completed successfully|
|201|Created|Entity was created successfully|
|400|Bad request|The Operation requested is improperly formatted - typically a validation failure.
|403|Forbidden|User does not have permission to perform the action|
|404|Not found|Entity not found|
|408|Request timeout|The client should resubmit the request|
|409|Conflict|The entity that you tried to add already exists|
|412|Precondition failed|Indicates that the requested Operation would violate a business rule|


### Objects

#### Asset 
|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|`asset_type_code`|`string`|m|One of 'ENVELOPE', 'FACILITY', 'BUILDING', 'SITE','FLOOR', 'ROOM', COMPONENT, LANDPARCEL. Mandatory on create. May **not** be specified during update |
|`code`|`string`|(m)|Mandatory on create. May **not** be specified during update|
|`name`|`string`|(m)|Mandatory on create. Optional on update|
|`adm_path`|`string`|o||
|`func_loc_path`|`string`|(m)|Mandatory on create. May **not** be specified during update|
|`creation_date`|`string`|o||
|`address`|`string`|o||
|`geom`|`string`|o||
|`latitude`|`number`|o||
|`longitude`|`number`|o||
|`barcode`|`string`|o||
|`serial_number`|`string`|o||


### `PUT assets/{uuid}`
Creates a new Asset.

Accepts:  `CoreAssetDto`
```
e.g.
{
  "asset_type_code" : "ENVELOPE"
  "code" : ""
  "name" : "" 
  "adm_path" : ""
  "func_loc_path" : ""
  "creation_date" : ""
  "address" : ""
  "geom" : ""
  "latitude" : ""
  "longitude" : ""

  "barcode" : ""
  "serial_number" : ""
  ...
}
```

> Depending on the type of Asset (as defined by `asset_type_id`) more fields specific to this type of Asset may be added to the request entity.
>
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `PATCH assets/{uuid}`
Updates an existing Asset.

Accepts:  `CoreAssetDto`
```
e.g.
{
  "asset_type_code" : "ENVELOPE"
  "code" : ""
  "name" : "" 
  "adm_path" : ""
  "func_loc_path" : ""
  "creation_date" : ""
  "address" : ""
  "geom" : ""
  "latitude" : ""
  "longitude" : ""

  "barcode" : ""
  "serial_number" : ""

  ...
}
```

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 412

### `DELETE assets/{uuid}`
Deletes an Asset.  The Asset is not deleted but is marked as inactive.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 409, 412



### `GET asset/link/types`
Returns a list of defined external identifier types
Accepts: *Nothing*

Returns:
```
[
  {
    "uuid":
    "description"
  }
]
```

Status codes: 200

### `PUT  asset/link/{uuid}/to/{external_id_type}/{external_id}`
Links an external identifier of the specified type to the specified Asset. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 409, 412

### `DELETE asset/link/{uuid}/to/{external_id_type}/{external_id}`
Removes the specified external identifier of the specified type linked to the specified Asset. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 408, 412

### `asset/link/{uuid}/to/{external_id_type}` ###
Returns the link to the specified asset of the given external type. Use `GET asset/link/types` to list the external identifier types that exist on your system.

Accepts: *Nothing*

Returns: 
```
   <external_id>
```

Status codes: 200, 400, 403