#!/bin/bash

tag=${1:-local}
buildRoot=$PWD
localRepo=$(realpath ~/projects/opennms/repository/)
mavenOpts="-Dmaven.repo.local=$localRepo -Pbuild-docker-images-enabled -Ddocker.image.tag=${tag} -Dmaven.test.skip.exec=true"

mvnProjects=("parent-pom" "shared-lib" "minion-gateway" "platform" "minion" "notifications")
jibs=("minion-gateway:horizon-stream-minion-gateway:main" "rest-server:horizon-stream-api" "notifications:horizon-stream-notification")
dockers=("ui:horizon-stream-ui" "grafana:grafana-dev" "keycloak-ui:horizon-stream-keycloak-dev")
buildKits=("ui")

# Building maven projectss
for project in ${mvnProjects[*]}; do
  cd $buildRoot/$project
  echo "Building complete project from $projectDir using tag $tag"
  mvnd $mavenOpts -Ddocker.image.tag=$tag clean install
done

# Building maven projects based on jib
for project in ${jibs[*]}; do
  projectDir=$(echo $project | cut -d ':' -f 1)
  image=$(echo $project | cut -d ':' -f 2)
  echo "Building $image from $projectDir"
  projectModule=$(echo $project | cut -d ':' -f 3)
  cd $buildRoot/$projectDir
  echo $projectModule
  if [ -n "$projectModule" ]; then
    cd $projectModule
  fi
  mvnd $mavenOpts jib:dockerBuild -Dimage="opennms/$image:$tag"
done

for project in ${dockers[*]}; do
  projectDir=$(echo $project | cut -d ':' -f 1)
  image=$(echo $project | cut -d ':' -f 2)
  echo "Building $image from $projectDir"
  cd $buildRoot/$projectDir
  if printf '%s\0' "${buildKits[@]}" | grep -Fxqz -- $projectDir; then
    dockerArgs="buildx"
  fi
  docker $dockerArgs build . -t "opennms/$image:$tag"
done

cd $buildRoot

. load-images.sh "$tag"

kubectl apply -f kc/
# Make sure pod/deployment restarts fetch latest images
if [[ $tag = "latest" ]]; then
  cat dev/kubernetes.kafka.yaml | sed -e "s|image: opennms/\(.*\)|image: opennms/\1:latest|" | kubectl apply -f -
else
  cat dev/kubernetes.kafka.yaml | sed "s|imagePullPolicy: Never|imagePullPolicy: Always|g" | sed -e "s|image: opennms/\(.*\)|image: opennms/\1:$tag|" | kubectl apply -f -
fi