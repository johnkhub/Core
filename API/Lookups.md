API
==========

Status codes
-------------

|Code|Meaning|Explanation|
|----|-------|-----------|
|200|OK||
|201|Created||
|||


### `GET /lookups`
Accepts: *Nothing*
Returns: *Nothing*

`PUT lookups/code`
Accepts: 
```
{
  "name",
  "description",
  "owner",
  "table"
}
```
Returns: *Nothing* and status []


### `POST /lookups/code/{code}`
Accepts: 
```
{
  "k",
  "v"
  "description"
}
```
Returns: *Nothing*

### `DELETE /lookups/code/{code}/{k}`

`POST /lookups/code/{code}?k=""&v=""&description=""`
Accepts: 
```
[
  {
    "k",
    "v"
    "description"
  }
]
```
Returns: *Nothing*

### `DELETE /lookups/code/{code}/keys`
Accepts: 
```
[
  list of keys
]
```
Returns: *Nothing*
