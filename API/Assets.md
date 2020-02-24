Operations
==========


Operations
- Acquisitions adds assets (PCS, Acquisitions module(in Outsystems))
- Subdivision & Consolidation of assets  (Land Management module)
- Movement of assets in the hierarchy



### Status codes

|Code|Meaning|Explanation|
|----|-------|-----------|
|200|OK||
|201|Created||
|||


### `PUT assets/{uuid}`
Accepts:
```
{
  "asset_type_code"
  "code"
  "name"
  "adm_path"
  "func_loc_path"
  "creation_date"

   -- asset_identification
  "barcode" 
  "serial_number" 
}
```



### `PUT  asset/link/{uuid}/to/{external_id_type}/{external_id}`

### `DELETE asset/link/{uuid}/to/{external_id_type}/{external_id}`

### `PUT assets/{uuid}/location`
Accepts:
```
{
    "latitude",
    "longitude"
}
```
Returns: *Nothing*

### `DELETE assets/{uuid}/location`
Accepts: *Nothing
Returns: *Nothing*


### `PUT assets/{uuid}/geometry`
Accepts:
```
{
   "geom" 
}
```
Returns: *Nothing*


### DELETE
```

```



> We have and explicit endpoint for each type.  This allows us to only add endpoints when new types are added instead of changing existing code when new types are introduced.
>

### `PUT assets/{uuid}/envelope`
Accepts:
```
{
}
```
Returns: *Nothing*


### `PUT assets/{uuid}/facility`
Accepts:
```
{
}
```
Returns: *Nothing*



### `PUT assets/{uuid}/building`
Accepts:
```
{
}
```
Returns: *Nothing*

### `PUT assets/{uuid}/floor`
Accepts:
```
{
}
```
Returns: *Nothing*


### `PUT assets/{uuid}/room`
Accepts:
```
{
}
```
Returns: *Nothing*


### `PUT assets/{uuid}/component`
Accepts:
```
{
}
```
Returns: *Nothing*
