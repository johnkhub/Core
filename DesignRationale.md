Rationale
=========


There is a very definite focus on starting with the database design and having the database do as much as possible.  The core is tasked with a lot of data aggregation/manipulation that makes sense to do in the database.  Somewhat contrary to the traditional IMQS way of doing things we have:
* Stricter application of referential integrity
* Elaborate check constraints
* Use of aggregation functions (Postgres specific functionality)
* Triggers where they make sense
* Making use of database access permissions on entities within the database to provide among other things read-only access to data
  

This approach is naturally at odds with being database agnostic.  Experience here has taught me that there is little benefit in being so strictly agnostic as in practice we deploy exclusively to Postgres. 
One does need to be pragmatic though and not use constructs that cannot reasonably implemented in another database.

