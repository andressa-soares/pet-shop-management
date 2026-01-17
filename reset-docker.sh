#!/bin/bash

set -e

echo "Stopping containers and removing volumes..."
docker compose down -v

echo "Building images without cache..."
docker build --no-cache .

echo "Starting containers..."
docker compose up -d

echo "Done."
