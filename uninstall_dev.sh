#!/bin/bash

echo "Down balloon docker container..."

docker-compose down

echo "Remove docker balloon images..."

docker image rm --force docker-registry.teleserver.com.ar/browxy_balloon:latest

docker image rm --force browxy_balloon:latest

echo "DONE!!"