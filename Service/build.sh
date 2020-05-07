#!/bin/bash

#
# There is quite a lot going on here. We use a maven docker container to build jar files - refer to the online documentation for the maven container on docker hub.
# It is important to note that the user's .m2 folder is fed to the maven container. For this to work your settings.xml file must not move the repository folder 
# from the user's $HOME/.m2 folder
#
# If you don't specify a tag for the built services the current branch will be used. Note that this will not work on a Jenkins slave as it checks out a specific 
# revision and not a branch. When using this on a Jenkins slave you *must* provide the branch yourself
#

function dockerise() {
  docker_name=$1
  tag=$2

  # Use bash builtin printf and print to variable
  printf -v fqn "%s:%s" "$docker_name" "$tag"
    
  docker run -t -e "MAVEN_OPTS=-Xmx1024m" --rm --name my-maven-project -v "$(pwd)":/usr/src/mymaven -v ~/.m2:/root/.m2 -w /usr/src/mymaven maven:3.5.0-jdk-8 mvn -DskipTests -Pcontainerize clean install
  docker build -t "imqs/$fqn" .
  docker push "imqs/$fqn"
}

set -e

# Assign current branch as tag (removing whitespace)
tag=${1:-$(git rev-parse --abbrev-ref HEAD | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//')}

dockerise "asset-core-service" "$tag"
sudo chown -R `whoami` ./target