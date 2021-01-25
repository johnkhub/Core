java -Dlogback.configurationFile="logback-spring.xml" -Djavax.net.ssl.trustStore=dtpw -Djavax.net.ssl.trustStorePassword=dtpw_store_password -classpath asset-core-service-jar-with-dependencies.jar za.co.imqs.coreservice.imports.Importer "$1" "$2" "$3" "$4"

