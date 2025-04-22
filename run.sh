#!/bin/bash

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java 17 is installed
if ! java -version 2>&1 | grep -q "17"; then
    echo "Java 17 is not installed. Please install Java 17 first."
    exit 1
fi

# Clean and build the project
echo "Building the project..."
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful. Starting the application..."
    java -jar target/db-schema-crawler-1.0.0.jar
else
    echo "Build failed. Please check the errors above."
    exit 1
fi 