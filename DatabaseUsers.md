Database access control
=======================

We apply strict access control to objects at database level.  As part of the creation of the database some login users are created.

`normal_reader`, as the name suggests has read-only access to most tables and this is the user that one would generally use for a service to connect to the database if it only needs to consume data. Reporting springs to mind as such a use case.

In cases where write access is required a new login role will need to be created that has access to the (*only*) the required tables.

`importer` has the same read access as `normal_reader` but in addition has write access to the tables in `public`, `asset` and `dtpw`.    

Restricted schemas
------------------
No user has access to the tables in the `access_control` schema. One needs to use the supplied functions to modify data. Views are supplied to query the data.

Write access to the `audit` schema is via functions and views.

TODO: 
-----
* Add views to `access control`
* Add views to `audit`
* Can we upgrade to pg 12 so we can have stored procedures (that can be transactional)
* Passwords need to be generated and exported to our management system during the commissioning of our stack
