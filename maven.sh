#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./maven.sh <command>"
    exit 1
fi

readonly INPUT_DOT_FILE=src/main/resources/Nodes_10_Random.dot
readonly PROCESSORS=1

if [ "$1" = "main" ]; then
    mvn clean compile 
    mvn javafx:run
elif [ "$1" = "package" ]; then
    mvn clean package
    
    if [ $# -eq 3 ]; then
        java -jar target/scheduler.jar "$2" "$3"
    else
        java -jar target/scheduler.jar "${DOT_FILE}" "${PROCESSORS}"
    fi
else
    echo "Invalid command"
    exit 1
fi

