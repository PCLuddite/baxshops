#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
    export JAVA_HOME=$(readlink /etc/alternatives/javac)
    export JAVA_HOME=${JAVA_HOME%/*}
    export JAVA_HOME=${JAVA_HOME%/*}
fi
echo "JAVA_HOME=$JAVA_HOME"

for DIR in versions/*; do
    VERSION=${DIR#versions/}
    echo "Building version $VERSION"
    cp -vr $DIR/* .
    
    mvn install clean
    STATUS=$?
    if [[ $STATUS = 0 ]]; then
        if mvn package; then
            if [[ ! -d './bin' ]]; then
                mkdir './bin'
            fi
            cp -v './target/baxshops-3.0-SNAPSHOT.jar' "./bin/baxshops-3.0-bukkit$VERSION.jar"
        fi
    fi
    echo
    echo
done