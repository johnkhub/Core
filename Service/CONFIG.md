Building and deploying
=======================

Build
-----

The service is intended to be deployed in Docker. As such the mvn `containerize` profile should be selected.
It will run as part of teh `package` life-cyle phase.

The result is a single jar file.

Upon startup, the service will create an empty database if one does not exist.


Deploy
-----

### Commandline parameters


|Name                     |M/O(Default)     |Type     |Description |
|-------------------------|-----------------|---------|------------|
|in.container             |O (false)        |Java     |Is the service running inside a container or not             |
|logback.configurationFile|M                |Java     |Typically a link to a file hosted by the configuration Server|
|spring.profiles.active   |O (`production`) |Java     |Spring profile. For integration testing, specify `test` |
|n/a                      |M                |Argument |Configuration file - Typically a link to a file hosted by the configuration Server|
|server.port              |M                |Java     |HTTP port. **Must** be prefixed by `--`|

> Note that the profile may also be set as an environment variable `spring_profiles_active`
> The resolution or is 
> 1. Environment variable
> 2. Java property
> (3) hardcoded default (`production`) 

Example commandline
```
java 
    -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy" 
    -jar asset-core-service.jar "http://config/config-service/config/asset-core-service/1/asset-core-service-config.json" 
    --server.port=80
```

### Configuration file

|Name|Description|
|----|-----------|
|jdbc|JDBC connection parameters |
|configurationService|Host and port of the Configuration Service |
|serveruser|For use with inter-service auth|
|authService|Optional - allow for pointing calls to auth to another host and port|

Example 
```
"jdbc": {
      "idleConnectionTestPeriod": "60",
      "idleMaxAge": "240",
      "minConnectionsPerPartition": "1",
      "maxConnectionsPerPartition": "5",
      "partitionCount": "2",
      "acquireIncrement": "5",
      "statementsCacheSize": "100",
      "driverClass": "org.postgresql.Driver",
      "jdbcUrl": "jdbc:postgresql://localhost:5432/test_core",
      "username": "imqs",
      "password": "1mq5p@55w0rd"
    },

    "configurationService": {
      "host": "localhost",
      "port": "80"
    },

    "serveruser" : "anonymous",
    "authService" : "legendqa:8001"
  }
```