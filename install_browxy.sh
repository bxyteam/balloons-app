#!/bin/bash

if [[ $# -lt 2 ]]; then
    echo "Error: No args provided."
    echo "Usage: $0 <Dockerfile> <Docker Registry> <docker-compose file>"
    exit 1
fi

DOCKERFILE="$1"
DOCKER_REGISTRY="$2"

echo "Building Docker image"
docker build -f "$DOCKERFILE" -t browxy_balloon .

echo "Tag docker image"
docker build -f "$DOCKERFILE" -t "${DOCKER_REGISTRY}"/browxy_balloon:1.0 .

echo "Push Docker image"
sudo docker push "${DOCKER_REGISTRY}"/browxy_balloon:1.0

echo "Done"
