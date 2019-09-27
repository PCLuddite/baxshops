#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
    export JAVA_HOME=$(readlink /etc/alternatives/javac)
    export JAVA_HOME=${JAVA_HOME%/*}
    export JAVA_HOME=${JAVA_HOME%/*}
fi
TMP="baxshops$RANDOM"
echo "JAVA_HOME=$JAVA_HOME"
echo "TMP=$TMP"

compile() {
    local VERSION=$1
    
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
}

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

for DIR in versions/*; do
    VERSION=${DIR#versions/}
    echo "Building version $VERSION"
    stash $VERSION
    cp -vr $DIR/* .
    compile $VERSION
    unstash $VERSION
    echo
    echo
done

rm -r /tmp/$TMP