Meta-data
===============================

The Meta-data API provides information about the system that aids in integrating with and managing the Core Service.
Current functionality is limited, but is expected to grow significantly as use-cases emerge.

API
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


### `GET assets/meta/fdw/alias/{serveralias}/username/{username}/dbalias/{dbalias}`
Returns a Postgres Foreign Data Wrapper definition string.  The string is a pg_sql snippet that the calling service
can use to create a FDW on its side.

|Parameter     |Description|
|--------------|-----------|
|`serveralias` | The logical name used on the client side to identify the server hsoting the Core database|
|`username`    |The PG username  to be used on the client side|
|`dbalias`     |The alias  |

> * Note that the returned definition will be for the `normal_reader` role in the Core Database  
> * A password of `*******` is used in the returned text
> * We force all local (calling service) proxies of remote tables into the local public schema

Accepts: *Nothing*

Returns:
```
plain text string
```
Status codes: 200, 400, 404, 408

#### Example ####

```
http://localhost:8669/assets/meta/fdw/alias/core_host/username/core_user
```

yields

```
-- We are very restrictive  in how the fdw is constructed

-- 1. We force the use of the 'normal_reader' role as it is read-only and has restricted visibility

-- 2. We force all local proxies of remote tables into the local public schema


-- Add the extensions in use by the foreign database to avoid missing data types etc.

CREATE EXTENSION IF NOT EXISTS "plpgsql";

CREATE EXTENSION IF NOT EXISTS "ltree";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE EXTENSION IF NOT EXISTS "postgis";

CREATE EXTENSION IF NOT EXISTS "unaccent";

CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE EXTENSION IF NOT EXISTS postgres_fdw;

DROP SERVER IF EXISTS core_host CASCADE;

CREATE SERVER IF NOT EXISTS core_host FOREIGN DATA WRAPPER postgres_fdw 

	OPTIONS (host '172.19.0.2' ,dbname 'test_core');

-- NOTE: Needs to be retrieved via another channel.

CREATE USER MAPPING IF NOT EXISTS FOR core_user SERVER core_host

	OPTIONS (user 'normal_reader', password '*******');


IMPORT FOREIGN SCHEMA asset FROM SERVER core_host INTO public;

IMPORT FOREIGN SCHEMA public FROM SERVER core_host INTO public;

IMPORT FOREIGN SCHEMA information_schema FROM SERVER core_host INTO public;

IMPORT FOREIGN SCHEMA dtpw FROM SERVER core_host INTO public;
```
