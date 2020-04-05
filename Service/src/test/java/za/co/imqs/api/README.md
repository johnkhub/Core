API Tests
=========

These are the API level integration tests.  

Pre-conditions
--------------
1. An auth user with a name and password of `dev`
2. Being integration tests, they require soem of the services in the stack to be running. These would typically be started in Docker.
    * Auth
    * Router
    * Configuration Service
    * The Core Service itsel



Rules
-----
1. Load data either programmatically or through the REST endpoints. You need these endpoints in any case within a containerised environment as accessing 
the filesystem is tricky. loading files are tricky 

2. Note the use of special controllers and repository calls that are **guarded by the Spring profile type**. These are typically to **delete** 
data and therefore additional safeguards are required. 

3. Use programmatic config instead of a bunch of files as much as possible. It makes test more readable as the config is close to the tests.

Conventions
------------

* We have a class per API method - this keeps the classes small, the tests easy to read and makes it easier to spot **missing** tests.
* We have a method for every possible return code.
* Consider a test case for each field as well to test validation.

TODO
----

* Create a compose files for the tests
* Implement the unimplemented tests
* More rigorous checks on the exact error output where for instance a specific validation criterium is violated.