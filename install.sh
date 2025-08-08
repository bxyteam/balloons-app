#!/bin/bash

if [[ $# -lt 3 ]]; then
    echo "Error: No args provided."
    echo "Usage: $0 <Dockerfile> <Docker Registry> <docker-compose file>"
    exit 1
fi

DOCKERFILE="$1"
DOCKER_REGISTRY="$2"
DOCKER_COMPOSE_FILE="$3"

echo "Compiling..."
rm -rf target

mvn clean package

mvn clean install

echo "Creating runnable directory"
mkdir -p target/runnable

echo "Copying bindist/lib to runnable/lib"
cp -r target/bindist/lib target/runnable/lib

echo "Copying classes to runnable/classes"
cp -r target/classes target/runnable/classes

echo "Copying scripts to runnable"
cp -r target/classes/scripts/. target/runnable

echo "Remove files and folders"
rm -rf target/runnable/classes/scripts/web

echo "Building Docker image"
docker build -f "$DOCKERFILE" -t browxy_balloon .

echo "Tag docker image"
docker build -f "$DOCKERFILE" -t "${DOCKER_REGISTRY}"/browxy_balloon:latest .

echo "Running Docker container"
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d

echo "Done"
