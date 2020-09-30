Overview
==========
This document will help you understand the code. It mostly follows the package structure of the java code.

Start with DTO that is richly annotated
Import code follows
Imports call rest controllers so resuse all logic logging and validation
Imports are column name based
Naming conventions allows for simple pass through different layers


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
Conversion between dto and model
Audit logging proxy
Permissions
Exception mapping
Look at the permission thing - how dynamic is it really since we have enum based on db values? ENUM VALUES MUST BE WRITTEN TO DB AT STARTUP!


Models
======

Persistence
===========

Schemas
=======

ACL Policy
==========

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
    + **Reads the list of liquibase schema files included in the jar file** to feed to the SchemaManagement code (below)
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

