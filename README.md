
> You will find most of the documentation in [Confluence **tree**](https://imqssoftware.atlassian.net/wiki/x/IwAhVw).  As it stands this is a dense tough read, but there is a considerable amount of information available.


House Rules
===========

Refer to [Schema Rules](schemaRules.md) for the naming conventions we employ.


Getting Going
=============

1. Create a config file as described in [Config](Service/CONFIG.md)
2. Add the snippet to your docker compose file as described in [Config](Service/CONFIG.md)
3. Run docker-compose up - The service will create an empty database

At this point you can either import data or you can elect to restore a database

Importing data
--------------
Refer to the testimport integration test for how to do this (proper documentation to come)


Restoring a database
--------------------

A good way to get going is to load the DTPW dataset froma backup.  A backup may be found [here](TBD), but this will obviously be somewhat out of date by the time you read this.

To restore:
1. Create an empty database (matching the name in your config file)
2. Install the extensions - as a shortcut look at [this](Service/src/main/resources/public/00_init.sql)
3. Restore the database

The reason for this is that some of the database types are implemented by the extensions, so restoration is not possible without them being available.



Postgres extensions
-------------------

This makes use of Postgres extensions

* [`ltree`]( https://www.postgresql.org/docs/10/ltree.htmlextension)
* [`uuid`](https://www.postgresql.org/docs/10/uuid-ossp.html)
* [`unaccent`](https://www.postgresql.org/docs/10/unaccent.html)
* [`pg_trm`](https://www.postgresql.org/docs/12/pgtrgm.html)
* [`postgis`](https://postgis.net/install)


Building, Configuration
-----------------------

The steps and options related to building the service and setting configuration options can be found in [Config](Service/CONFIG.md)

APIs
====

Each one of the API groups is documentd in a separate markdown file. Note that in many cases there is a database level API and a REST API.  The database level API is there to make implementing services easier and to push down
more of the validation and integrity checking to the database level.

Security is managed as explained in [API Security](APISecurity.md) for REST interfaces and in [Database Users](DatabaseUsers.md) for database level interfaces as would be the case for a service imnplementing the REST APIs.


Core API
--------

Deals with CRUD operations on Assets as well annotating them with basic meta-data.

* [Asset API definitions](API/Assets.md) 
* [Asset Tagging definitions](API/Tagging.md)

Lookups
---------

* [Lookups API](API/Lookups.md)


Access Control
--------------

Deals with managing granular access to entities. Note that the concept if an entity extends beyond just an assets.

* [Access Control API](API/AccessControl.md)

Audit
-------

* [Audit Logging API](API/Audit.md)


Diagnostics & Debugging
========================

* [Cookbook]Service/COOKBOOK.md) contains sql query examples that helps identitify problems and validate data.

Code Review Guidelines
======================

Database
--------

* Naming conventions
* Foreign keys
* NULL / NOT NULL
* CHECK constraints
* Avoid triggers
* Default values
* Indexing

Code
-----
* Transactions
* Error reporting
* Authorisation/Authentication
* Audit logging

