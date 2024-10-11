#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./maven.sh <command>"
    exit 1
fi

readonly INPUT_DOT_FILE=src/main/resources/dotfiles/input/Nodes_11_OutTree.dot
#readonly INPUT_DOT_FILE=src/test/resources/dotfiles/crawled/Fork_Join_Nodes_10_CCR_10.03_WeightType_Random_Homogeneous-4.dot
readonly PROCESSORS=4

if [ "$1" = "main" ]; then
    mvn clean compile package
    
    if [ $# -eq 3 ]; then
        java -jar -Xmx4G target/scheduler.jar "$2" "$3"
    else
        java -jar -Xmx4G target/scheduler.jar "${INPUT_DOT_FILE}" "${PROCESSORS}" -v
    fi
else
    echo "Invalid command"
    exit 1
fi

