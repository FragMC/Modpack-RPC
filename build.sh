#!/bin/bash

echo "====================================="
echo "Building VanillaRPC..."
echo "====================================="
echo

chmod +x gradlew
./gradlew clean build

echo
echo "====================================="
echo "Build complete!"
echo "Your mod is in: build/libs/"
echo "====================================="
