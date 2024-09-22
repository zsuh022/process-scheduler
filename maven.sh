#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./maven.sh <command>"
    exit 1
fi

if [ "$1" = "main" ]; then
    mvn clean compile 
    mvn javafx:run
elif [ "$1" = "package" -a $# -eq 3 ]; then
    mvn clean package
    java -jar target/scheduler.jar "$2" "$3"
else
    echo "Invalid command"
    exit 1
fi

