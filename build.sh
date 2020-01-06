#
#  Copyright (C) Timothy Baxendale
# 
#  This library is free software; you can redistribute it and/or
#  modify it under the terms of the GNU Lesser General Public
#  License as published by the Free Software Foundation; either
#  version 2.1 of the License, or (at your option) any later version.
# 
#  This library is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  Lesser General Public License for more details.
# 
#  You should have received a copy of the GNU Lesser General Public
#  License along with this library; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
#  USA
#
#!/bin/bash

if [ "$JAVA_HOME" = "" ]; then
    export JAVA_HOME=$(readlink /etc/alternatives/javac)
    export JAVA_HOME=${JAVA_HOME%/*}
    export JAVA_HOME=${JAVA_HOME%/*}
fi
SHOPS_VER="1.0b3"
VERSION='1_13'
echo "JAVA_HOME=$JAVA_HOME"
echo "SHOPS_VER=$SHOPS_VER"


compile() {    
    echo "Building version $VERSION..."
    
    mvn install clean
    STATUS=$?
    if [[ $STATUS = 0 ]]; then
        if mvn package; then
            if [[ ! -d './bin' ]]; then
                mkdir './bin'
            fi
            cp -v "./target/baxshops-${SHOPS_VER}-bukkit${VERSION}-RELEASE.jar" "./bin/baxshops-${SHOPS_VER}-bukkit${VERSION}-RELEASE.jar"
        fi
    fi
    
    echo "Done."
    return $SATUS
}

compile
exit $?
