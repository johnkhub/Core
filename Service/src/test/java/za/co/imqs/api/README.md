API Tests
=========

These are the API level integration tests.  

Pre-conditions
--------------
1. An auth user with a name and password of `dev`. You will have this out of the box with the default docker deployment
2. Being integration tests, they require some of the services in the stack to be running. These would typically be started in Docker.
    * Auth
    * Router
    * Configuration Service
    * The Core Service itself

Rules
-----
1. Load data either programmatically or through the REST endpoints. You need these endpoints in any case within a containerised environment as accessing 
the filesystem is tricky.
>Using the APIs to populate data is awkward at first since you're bootstrapping the system but it does lead to better test coverage during development 
>as you are using the infrastructure to test the infrastructure 
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
* More rigorous checks on the exact error output where for instance a specific validation criterion is violated.
* Create a docker file to start up these services and a test rule to call the docker file  