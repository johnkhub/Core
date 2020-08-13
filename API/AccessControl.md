Access Control
===============================

There are two levels of security the first is that which is enforced by the database via functions.  In and of itself it is not completely secure.  The functions must be correctly invoked by the service layer APIs to secure the system.


Database level
---------------

|Function                                 |Description       |
|-----------------------------------------|------------------|
|`access_control.fn_get_audit_root_key() RETURNS uuid;` | Not yet sure how we will generate this value or manage it. This function is a way to abstract this indecision from the audit log implementation|
|`access_control.fn_get_effective_access(p uuid, e uuid) RETURNS integer`|Given the principal `p` calculates the effective access (considering direct permissions and those via groups) to the  entity `e`'|
|`access_control.fn_get_effective_grant(p uuid, e uuid) RETURNS integer` |Given the principal `p` calculates the effective access that my be granted to another (considering direct permissions and those via groups) to the  entity `e`|
|`access_control.sp_grant_access(grantor uuid, access_mask integer, to_entities uuid[], for_principal uuid) RETURNS void`|The `grantor` grants `access_mask` to the entities in the array `to_entities[]` to principal `to_principal`
|`access_control.sp_revoke_access(revoker uuid, to_entities uuid[], for_principal uuid) RETURNS void `|Revoke the access of principal `for_principal` to all entities (e.g. asset_ids) in the list `to_entities uuid`|
|`access_control.sp_add_group(code varchar(10), description text DEFAULT NULL)  RETURNS void`|Add a new Group with the **name** `code` |
|`access_control.sp_remove_group(code varchar(10))  RETURNS void`|Removes the Group with **name** `code`. Not allowed to do so if there are still users attached|
|`access_control.sp_add_user(user_id uuid, code varchar(10), description text DEFAULT NULL)  RETURNS void`|Adds a new User. We must specify the uuid instead of the code as we need to use the identity from Auth.|
|`access_control.sp_remove_user(code varchar(10))  RETURNS void`|Removes the User with **name** `code`|
|`access_control.sp_add_user_to_group(user_id uuid, group_name varchar(10))  RETURNS void`||
|`access_control.sp_remove_user_from_group(user_id uuid, group_name varchar(10))  RETURNS void`||;



API level
----------

## Security

See [API Security](APISecurity.md) for an overview of how security is implemented and the requirements of making secured REST calls.
The current implementation of this API: 
* *Currently* only supports token based authentication, **not** inter-service authentication
* *Currently* only enforces authentication and **not** authorisation


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


#### UserDto ####

|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|`principal_id`|`string`|m|UUID|
|`name`|`string`|m|Name of user|
|`description`|`string`|o|Description|
|`reserved`|`boolean`|o|User is a system reserved User|

#### GroupDto ####

|Field  |Type  |o/m  |Description|
|-------|------|-----|-----------|
|`group_id`|`string`|m|UUID|
|`name`|`string`|m|Name of user|
|`description`|`string`|o|Description|
|`reserved`|`boolean`|o|Group is a system reserved Group|


### `GET assets/access/user`  (NOT IMPLEMENTED)
Returns the complete list of Users.

Accepts: *Nothing*

Returns:
```
[UserDto]
```
Status codes: 200, 400, 403, 408


### `GET assets/access/group`  (NOT IMPLEMENTED)
Returns the complete list of Groups.

Accepts: *Nothing*

Returns:
```
[GroupDto]
```
Status codes: 200, 400, 403, 408

### `GET assets/access/group/{name}`
Returns the Group with the supplied name.

Accepts: *Nothing*

Returns:
```
GroupDto
```
Status codes: 200, 400, 403, 408



### `POST assets/authorisation/users`
Creates a new User.

Accepts: 
```
UserDto
```
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412

### `DELETE assets/authorisation/users/{user_uuid}`
Removes the User with the supplied User UUID.

Accepts: *Nothing* 
Returns: *Nothing*
Status codes: 200, 400, 403, 408, 412



### `DELETE assets/access/groups/{group_uuid}`
Removes the Group with the supplied Group UUID.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 404, 408, 412

###  `POST assets/access/groups/{group_uuid}`
Creates a new Group with the specified UUID.

Accepts: 
```
GroupDto
```
Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `POST assets/access/groups/{group_name}/{user_uuid}`
Adds the User with the specified UUID to the Group with the specified name. The user must exist.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 404, 408, 412


### `DELETE assets/access/groups/{group_name}/{user_uuid}`
Removes the User with the specified UUID from the Group with the specified name. 

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 404, 408, 412


### `POST assets/access/entity/{entity_uuid}/{grantee_uuid}/{perms}`
Grants permissions `perms` for the specified entity to the user with UUID `grantee_uuid`.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 201, 400, 403, 408, 409, 412


### `DELETE assets/access/entity/{entity_uuid}/{revokee_uuid}`
Revokes permissions `perms` for the specified entity to the user with UUID `revokee_uuid`.

Accepts: *Nothing*

Returns: *Nothing*

Status codes: 200, 400, 403, 404, 408, 412