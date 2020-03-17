
Tagging
==============

There are two levels of security the first is that which is enforced by the database via functions.  In and of itself it is not completely secure. The functions must be correctly invoked by teh service layer 
APIs to secure the system.


Database level
---------------

|Function                                                    |Description       |
|------------------------------------------------------------|------------------|
|`public.fn_add_tags(asset uuid, tags text[]) RETURNS void;` | Tags the specified asset witch each of the specified tags.  The tags must exist in `public.asset_tags`.|
|`public.fn_has_tag(asset uuid, tag text) RETURNS boolean`   | Returns true iff the specified asset is tagged with the specified tag. |

Examples
--------

```
INSERT INTO asset_tags (k,v) VALUES ('AT_RISK', 'Land parcel is at risk');
```

```
INVOKE public.fn_add_tags('64fe52b9-4cdc-4cf9-aaca-b57a158b5693', ARRAY('AT_RISK'));
```

```
SELECT tags FROM asset_tags WHERE asset_id = '64fe52b9-4cdc-4cf9-aaca-b57a158b5693');
```

```
SELECT (SELECT tags FROM asset_tags WHERE asset_id = '64fe52b9-4cdc-4cf9-aaca-b57a158b5693'') = ARRAY['AT_RISK']
```
*OR*
```
SELECT public.fn_has_tag( '64fe52b9-4cdc-4cf9-aaca-b57a158b5693'', 'AT_RISK')
```


API level
----------

To use this API you must be authenticated via the Auth service and have permissions to manage security on the Asset system. Both token-based or interservice authentication needs to be supported on all endpoints.


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


### `GET assets/{uuid}/tag`   
Returns all of the tags linked to teh specified asset.

Accepts: 
*Nothing**
```
Returns:
```
[
    'tag1'
    'tag2'
    ...
    'tagn'
]
```
Status codes: 200, 400, 403, 408

### `GET assets/{uuid}/tag/{tag}`   
Accepts: 
*Nothing**
```
Returns:
```

[
    true or false
]

```
Status codes: 200, 400, 403, 408


### `PUT assets/{uuid}/tag/{tag1}?tag2&tag3&tag4`
Note the use of keys without parameters for the trailing tags.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `DELETE assets/{uuid}/tag/{tag}`
Accepts: *Nothing* 
Returns: *Nothing*
Status codes: 200, 400, 403, 408, 412

