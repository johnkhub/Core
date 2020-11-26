set FAKEAUTH=true
java -Dlogging.config="logback-spring.xml" -Dspring.profiles.active=test -jar asset-core-service.jar --server.port=8669 --config=file:config.json