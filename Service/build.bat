--mvn clean package -Pcontainerize
docker build -t "imqs/asset-core-service:master" .
docker push "imqs/asset-core-service:master"