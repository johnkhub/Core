API
====


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

#### Lookup type
|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|``|``|||
|``|``|o||
|``|``|o||

#### Lookup value
|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|``|``|||
|``|``|o||
|``|``|o||


### `GET /lookups`
Accepts: *Nothing*

Returns: [Lookup value]
Status codes: 200, 400, 403, 408

### `PUT lookups/code`
Accepts: 
```
{
  "name",
  "description",
  "owner",
  "table"
}
```
Returns: *Nothing* 

Status codes: 201, 400, 403, 408, 409, 412


### `POST /lookups/code/{code}`
Accepts: 
```
{
  "k" : "",
  "v" : "",
  "description" : ""
}
```
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `DELETE /lookups/code/{code}/{k}`
Delete the lookup value with the key (`k`) from the type with teh specified code.


Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `POST /lookups/code/{code}?k=""&v=""&description=""`
Adds a lookup value to the type of the specified code.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `DELETE /lookups/code/{code}/keys`
Deletes the lookup values of the specified code with the supplied key (`k`) values..

Accepts: 
```
[
  list of keys
]
```
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 412

