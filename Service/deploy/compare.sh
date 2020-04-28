#!/bin/bash
java -Dlogback.configurationFile=/home/frank/Development/Core/Service/deploy/logback.groovy -Dspring.profiles.active=production -jar ./target/asset-core-service.jar --server.port=8669 --compare-schemas jdbc:postgresql://localhost:5432/core12feb_2 imqs 1mq5p@55w0rd --config=file:/home/frank/Development/Core/Service/deploy/config.json

