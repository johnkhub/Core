
Access Control
==============

There are two levels of security the first is that which is enforced by the database via functions.  In and of itself it is not completely secure. The functions must be correctly invoked by teh service layer 
APIs to secure the system.


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
|`access_control.sp_add_remove_user_from_group(user_id uuid, group_name varchar(10))  RETURNS void`||;



API level
----------

To use this API you must be authenticated via the Auth service and have permissions to manage security on the Asset system. Both token-based or interservice authentication needs to be supported on all endpoints.


### Status codes

|Code|Meaning|Explanation|
|----|-------|-----------|
|200|OK||
|201|Created||
|||



### `GET assets/authorisation/users`   
Accepts: 
```
    {
        "name", 
        "user_uuid"
    } 
```
Returns:
```
[
    {

    }
]
```


### `POST assets/authorisation/users`
Accepts: 
```
{
    "name",
    "user_uuid"
}
```
Returns: *Nothing*

### `DELETE assets/authorisation/users/{user_uuid}`
Accepts: *Nothing* 
Returns: *Nothing*

### `GET assets/authorisation/users`
Accepts: *Nothing*
Returns:
```
[
    {

    }
]
```

### `POST assets/authorisation/groups`
Accepts: 
```
    {
        "name",
        "group_uuid"
    }
```
Returns: *Nothing*

### `DELETE assets/authorisation/groups/{group_uuid}`
Accepts: 

###  `POST assets/authorisation/groups/{group_uuid}`
Accepts: 
```{
    "user_uuid",
    "group_uuid"
    }
```
Returns: *Nothing*

### `DELETE assets/authorisation/groups/{group_uuid}/{user_uuid}`
Accepts: *Nothing*
Returns: *Nothing*

### `POST assets/authorisation/entity/{entity_uuid}`
Accepts: 
```
    {
        "principal_uuid",
        "access_type",
        "grant_type"
    }
```
Returns: *Nothing*

### `POST assets/authorisation/entity/{entity_uuid}/{principal_uuid}`
Accepts: *Nothing*
Returns: *Nothing*

### `DELETE assets/authorisation/entity/{entity_uuid}/{principal_uuid}`
Accepts: *Nothing*
Returns: *Nothing*