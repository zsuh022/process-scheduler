#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./maven.sh <command>"
    exit 1
fi

if [ "$1" = "main" ]; then
    mvn clean compile 
    mvn javafx:run
elif [ "$1" = "package" ]; then
    mvn clean package
    java -jar target/scheduler.jar
else
    echo "Invalid command"
    exit 1
fi

