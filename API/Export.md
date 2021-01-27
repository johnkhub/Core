Export
==========

This ReST enpoint allows for the entire Core Directory to be downloaded as a zipped `.csv` file.  It makes use of `dtpw.dtpw_export_view`.
If you have direct access to the database, saving the output of `SELECT * FROM dtpw.dtpw_export_view ORDER BY func_loc_path ASC` to file will 
be significantly faster than doing it through this endpoint.



API
---

### API Versioning

The draft [API versioning] (https://imqssoftware.atlassian.net/wiki/x/eoCZZw) system is has not yet been implemented.

### Rules

[Rules and conventions ](https://imqssoftware.atlassian.net/wiki/x/S4CBeQ)

### Security

See [API Security](APISecurity.md) for an overview of how security is implemented and the requirements of making secured REST calls.

The current implementation of this API: 
* *Currently* only supports token based authentication, **not** inter-service authentication
* *Currently* only enforces authentication and **not** authorisation

### Status codes

|Code|Meaning            |Explanation                                                                     |
|----|-------------------|--------------------------------------------------------------------------------|
|200 |OK                 |Operation completed successfully                                                |
|403 |Forbidden          |User does not have permission to perform the action                             |
|408 |Request timeout    |The client should resubmit the request                                          |


### Objects

None

### `GET download/export`

Downloads the output of `SELECT * FROM dtpw.dtpw_export_view ORDER BY func_loc_path ASC` as a zipped csv file.
> This is **SLOW**. Really slow. Sorry.

Accepts:  *Nothing*

Returns: *Nothing*

Status codes: 200, 403, 408

