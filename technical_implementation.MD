Overview
==========
This document will help you understand the code. It mostly follows the package structure of the java code.

Key to understanding things is understanding the relationship between the *richly annotated* DTOs how they are converted to/from Models and how the same is handled in the persistence layer.
Data is imported via the ReST endpoints and uses the same DTOs to achieve this. Naming conventions on classes and field names allow for simple pass through different layers.

The specific benefits of this approach are 
* That there is only one set of logic that needs to be applied (instead of separate implementations for ReST and imports)
* Adding a field to the automatically makes it importable from teh flat csv file




Entity identifier and Access control
====================================

Caveats
========
Naming conventions of fields
Resources folder structure must be adhered to


DTOs and ORM
============
DB must habve a type field to manage mapping


Controllers
===========

Responsibilities:
1. Conversion between DTOs and Models -
2. Audit logging proxy - 
3. Permissions -
4. Exception mapping -

> Notes:
> 1. Look at the permission thing - how dynamic is it really since we have enum based on db values? ENUM VALUES MUST BE WRITTEN TO DB AT STARTUP!


Models
======


Persistence
===========



Schemas & static data
=====================

[Database Schema Management](Service/src/main/resources/README.md)


ACL Policy
==========
TODO


Spring Configuration and Miscellaneous
======================================

Generic
-------

* AuditConfiguration
    + No surprises
    + Mostly deals with audit schema data source
* AuthConfiguration
    + Configures auth interceptor
    + **Has a mode that disables auth** (in `test` profile)
    + Deals with auth schema data source
* MapperConfiguration
    + No surprises
    + Adds Joda support to Jackson
* ServiceConfiguration
    + Miscellaneous configuration beans
    + Reads scheduled tasks from config file
* ServiceHealthConfiguration
    + No surprises
    + Installs global exception handler that will make ping return an error

Implementation Specific 
------------------------

* PersistenceConfiguration
    + Mostly deals with data sources used by the actual application (as opposed to audit and authorisation that I regard as supporting functionality)
    + **Reads the list of liquibase schema files included in the jar file** to feed to the SchemaManagement code (below). Refer to [Database Schema Management](Service/src/main/resources/README.md).
* WebMvcConfiguration
    + **Sets URL paths for Controllers**

* Boot & commandline handling
    + **Contains the `@ComponentScan` annotation**
    + **Handles Spring profile**
    + Feature flags
* SchemaManagement
    + A CLIHandler that uses **Liquibase** to manage database schemas
* StartupListener
    + The final bit of Spring to start up - once everything has been initialised
    + Handles the processing of the commandline ina generic way
    + **There is a coded dependency on the handlers**. See Clihandler interface and implementations. The handler mechanism could use some work.


Problems
========

Liquibase
---------

* Check constraints

