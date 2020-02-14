Operations
==========


Operations
- Acquisitions adds assets (PCS, Acquisitions module(in Outsystems))
- Subdivision & Consolidation of assets  (Land Management module)
- Movement of assets in the hiererachy




```
PUT assets/{uuid} 
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


```
PUT  asset/link/{uuid}/to/{external_id_type}/{external_id}
DELETE asset/link/{uuid}/to/{external_id_type}/{external_id}
```



```
PUT assets/{uuid}/location

{
    "latitude",
    "longitude"
}

DELETE assets/{uuid}/location
```



```
PUT assets/{uuid}/geometry
{
   "geom" 
}

DELETE
``

```
-- explicit endpoint for each type so we then need to only add endpoints instead of changing -- -- existing code when new types are introduced

```
PUT assets/{uuid}/envelope
{
}
```

```
PUT assets/{uuid}/facility
{
}
```


```
PUT assets/{uuid}/building
{
}
```


```
PUT assets/{uuid}/floor
{
}
```


```
PUT assets/{uuid}/room
{
}
```

```
PUT assets/{uuid}/component
{
    "component_type"
}
```
