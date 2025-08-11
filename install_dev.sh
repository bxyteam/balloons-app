#!/bin/bash

sudo rm -r /var/compiler/balloon/data

./install.sh Dockerfile.dev docker-registry.teleserver.com.ar docker-compose.dev.yml
