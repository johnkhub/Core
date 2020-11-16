#-------------------------------------------------------------------------------
#
#  Pre-conditions
#	1) Ports are to be set in the compose file
#	2) imqsvar to be mapped in the compose file
#	3) Compose file to define the configuration service as "config-service"
#
#-------------------------------------------------------------------------------

#
# This is the boiler-plate stuff
#
FROM imqs/java-base:8

#Inside the container everything runs on port 80
EXPOSE 80 12000

#
# Get the build artefacts and copy them into the image
#
WORKDIR /usr/lib/imqs
COPY /target/asset-core-service.jar .
COPY /target/asset-core-service-jar-with-dependencies.jar .
COPY /deploy/import.bat .
COPY /deploy/import.sh .
COPY /deploy/logback.groovy .
COPY /deploy/index.html .
COPY /deploy/import_config.json .
COPY /deploy/core_apple.jpg .
COPY /deploy/export.png .

RUN chmod -R 777 /usr/lib/imqs

# Define the entry point. Note the use of exec. This causes the child process to replace the launching process - required for proper signal handling.
ENTRYPOINT exec java -agentlib:jdwp=transport=dt_socket,address=12000,server=y,suspend=n -Din.container=true -Dlogback.configurationFile="http://config/config-service/config/asset-core-service/1/logback-asset-core-service.groovy" -jar /usr/lib/imqs/asset-core-service.jar --server.port=80  --config="http://config/config-service/config/asset-core-service/1/asset-core-service-config.json" 
