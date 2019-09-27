#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
    export JAVA_HOME=$(readlink /etc/alternatives/javac)
    export JAVA_HOME=${JAVA_HOME%/*}
    export JAVA_HOME=${JAVA_HOME%/*}
fi
TMP="baxshops$RANDOM"
echo "JAVA_HOME=$JAVA_HOME"
echo "TMP=$TMP"

stash() {
    local VERSION=$1
    if [[ ! -d "/tmp/$TMP/$VERSION" ]]; then
        mkdir -p "/tmp/$TMP/$VERSION"
    fi
    for FILE in $(find "versions/$VERSION/." -type f); do
        cp --parents -v "${FILE#versions/$VERSION/}" "/tmp/$TMP/$VERSION/"
    done
}

unstash() {
    local VERSION=$1
    cp -vr /tmp/$TMP/$VERSION/* .
}

compile() {
    local VERSION=$1
    
    echo "Building version $VERSION..."
    
    stash $VERSION
    
    cp -vr versions/$VERSION/* .
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
    
    unstash $VERSION
    
    echo "Done."
}

if [[ "$1" != "" ]]; then
    VERSION="$1"
    if [[ ! -d "versions/$VERSION" ]]; then
        echo "No version $VERSION to build"
        exit 1
    fi
    compile $VERSION
else
    for DIR in versions/*; do
        VERSION=${DIR#versions/}
        compile $VERSION
        echo
    done
fi

rm -r /tmp/$TMP