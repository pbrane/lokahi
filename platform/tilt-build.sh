#!/usr/bin/env bash

cd platform

IMAGE_TAG="local"

if [ -n "$1" ]; then
  IMAGE_TAG=$(echo "$1" | cut -d : -f 2)
  echo "Applying custom image tag ${IMAGE_TAG}."
fi

mvn install -Pbuild-docker-images-enabled -DskipTests -Ddocker.image.tag=${IMAGE_TAG}
