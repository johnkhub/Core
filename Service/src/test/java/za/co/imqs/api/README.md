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
1. Try not to load data in ways other than through the REST endpoints you need in any case
With containerisation loading files are tricky and you need the REST endpoints anyway so you might as well use them for testing as well.

2. Note the use of special controller and repository calls that are **guarded by the Spring profile type**. These are typically to delete 
data and therefor additional safeguards are required. 


Conventions
------------

* We have a class per API method.
* We have a method for every possible return code.
* Consider a test case for each field as well to test validation.

TODO
----

* Create a compose files for the tests
* Implement the unimplemented tests
* More rigorous checks on the exact error output where for instance a specific validation criterium is violated.