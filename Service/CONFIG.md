Building and deploying
=======================

Build
-----

The service is intended to be deployed in Docker. As such the mvn `containerize` profile should be selected.
It will run as part of the `package` life-cycle phase.

The result is a single jar file.

> [build.sh](build.sh) is a shell script that will build the docker image and push to DockerHub. The image will be tagged with the *current* branch.

Upon startup, the service will create an empty database if one does not exist.

Developer
---------
* Deploy has some sample configuration and launch scripts to run the service
* Use [this](composefile_snippet.yaml) to add to your compose file
* [This](src/test/java/za/co/imqs/api/README.md) provides some info on running tests 
* The docker configures the service for remote debugging. All you need to do is to expose the port in your compose file.


Run
-----

### Commandline parameters


|Name                     |M/O (Default)     |Type     |Description |
|-------------------------|-----------------|---------|------------|
|in.container             |O (false)        |Java     |Is the service running inside a container or not             |
|logback.configurationFile|M                |Java     |Typically a link to a file hosted by the configuration Server|
|spring.profiles.active   |O (`production`) |Java     |Spring profile. For integration testing, specify `test`      |
|server.port              |M                |Argument |HTTP port                                                    |
|sync-schemas             |O                |Argument |See Schema Management section                                |
|document-schemas         |O                |Argument |See Schema Management section                                |
|compare-schemas          |O                |Argument |See Schema Management section. Requires extra parameters for connection to the other database (see example)|
|manual-schemas           |O                |Argument |See Schema Management section                                |
|config                   |M                |Argument |Configuration URI - Typically a link to a file hosted by the configuration Server| 

>The config parameter **MUST** be the final argument     
 
> Note that the profile may also be set as an environment variable `spring_profiles_active`
> The resolution order is 
> 1. Environment variable
> 2. Java property
> 3. Hardcoded default (`production`) 

Example commandline
```
java 
    -Din.container=true
    -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy"
    -spring.profiles.active=production 
    -jar asset-core-service.jar  
    --server.port=8669

    --config="http://config/config-service/config/asset-core-service/1/asset-core-service-config.json"
```

```
java 
    -Din.container=true
    -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy"
    -spring.profiles.active=production 
    -jar asset-core-service.jar  
    --server.port=8669

    --compare-schemas jdbc:postgresql://localhost:5432/core12feb_2 imqs 1mq5p@55w0rd
    
    --config="http://config/config-service/config/asset-core-service/1/asset-core-service-config.json"
```

```
java 
    -Din.container=true
    -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy"
    -spring.profiles.active=production 
    -jar asset-core-service.jar  
    --server.port=8669

    --sync-schema

    --config=file:/home/frank/Development/Core/Service/src/test/resources/config.json
```

```
java 
    -Din.container=true
    -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy"
    -spring.profiles.active=production 
    -jar asset-core-service.jar  
    --server.port=8669

    --manual-schemas

    --config=file:/home/frank/Development/Core/Service/src/test/resources/config.json
```

#### Native vs Docker
**Native**
* `in.container` removed or set to `false`
* `--config` refers to `localhost:2010` e.g. `--config="http://config/config-service/config/asset-core-service/1/asset-core-service-config.json"`

### Configuration file

|Name|Description|
|----|-----------|
|jdbc|JDBC connection parameters |
|configurationService|Host and port of the Configuration Service |
|serveruser|For use with inter-service auth|
|authService|Optional - allow for pointing calls to auth to another host and port|
|sql-schedules|Schedules SQL execution according to cron schedules|

##### Example 
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

This is a very basic mechanism and intended for simple tasks such the one illustrated here: updating
materialized views.

```
   "sql-schedules" : [
    {
      "name" : "refresh_core_report_view",
      "description" : "",
      "sql" : "REFRESH MATERIALIZED VIEW dtpw.dtpw_core_report_view;",
      "cron" : "0 */5 * * * *"
    },
    {
      "name" : "refresh_ei_report_view",
      "description" : "",
      "sql" : "REFRESH MATERIALIZED VIEW dtpw.dtpw_ei_report_view;",
      "cron" : "0 */5 * * * *"
    }
  ]
```
In this example the (verbatim) sql will execute every 5 minutes. **Note** the use of the schema name.

Schema management
-------------------

Some aspects of schema management are exposed via commandline interface.
>Note that this is mainly for development purposes.

`sync-schemas` -  This is used to take on a database that did not previously have liquibase. Use compare schemas (below) to generate a report on the differences between the databases.
Then, generate scripts to update the database. I do this by hand. Liquibase should in principle also be able to do this, but I have not looked at this yet. Once the databases are the same 
use this command to generate the SQL that will generate the required changelog data in the database to bootstrap liquibase. **NOTE:**  the changelog tables in the database must be empty.
The output is sent to the service trace log. 
    
`document-schemas` - Generates javadocs-like documentation for the database.
 
`compare-schemas` - Compares an external database to the one managed by the service and generates a difference report - outputs to the service trace log. 

`manual-schemas` - This option disables the internal management of the database schemas. This allows you to modify the schema by and and still get the service to start up. It should be used wit caution as reverting back to internal management will require developer involment to execute the `compare-schemas` and `sync-schemas` operations.
