#!/bin/sh
#
# Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
#

set -e
[ "$DEBUG" = "true" ] && set -x

# redeclaration of env variables so that editors do not think every variable is a typo
WAR_PATH=$WAR_PATH
MAX_HEAP=$MAX_HEAP
TRIES_COUNT=$TRIES_COUNT
CATALINA_HOME=$CATALINA_HOME
PORT=$PORT
SKIP_DEPLOY=$SKIP_DEPLOY
APPLICATION_NAME=$APPLICATION_NAME
CONTEXT_ROOT=$CONTEXT_ROOT
MEMORY_LEAK_PREVENTION=$MEMORY_LEAK_PREVENTION
DIST_DIR=$DIST_DIR
SKIP_START_STOP=$SKIP_START_STOP
JVM_ARGS=$JVM_ARGS
SKIP_CHECK=$SKIP_CHECK

if [ "$CATALINA_HOME" = "" -o "$WAR_PATH" = "" -o "$MAX_HEAP" = "" -o "$TRIES_COUNT" = "" ]; then
    echo ARGUMENTS NOT OK
    exit 1
fi

start_tomcat() {
    if [ "$SKIP_CHECK" != "true" ] && jps -v | grep 'jersey.config.test.memleak.tomcat.magicRunnerIdentifier'; then
        echo ERROR There is already running instance of Tomcat
        exit 2
    fi

    if nc -z localhost $PORT; then
        echo ERROR port $PORT is not free!
        exit 3
    fi

    [ -d "$CATALINA_HOME"/webapps/"$CONTEXT_ROOT" ] && rm -rf "$CATALINA_HOME"/webapps/"$CONTEXT_ROOT"
    [ -f "$CATALINA_HOME"/webapps/"$CONTEXT_ROOT".* ] && rm -f "$CATALINA_HOME"/webapps/"$CONTEXT_ROOT".*

    sed -i -e 's@\(Connector port="\)[0-9]*\(" protocol="HTTP/1.1"\)@\1'$PORT'\2@' "$CATALINA_HOME"/conf/server.xml

    if [ "$MEMORY_LEAK_PREVENTION" != "true" ]; then
        sed -i -e 's@\(^[^<].*org.apache.catalina.core.ThreadLocalLeakPreventionListener.*$\)@<!--\1-->@' "$CATALINA_HOME"/conf/server.xml
        sed -i -e 's@\(^[^<].*org.apache.catalina.core.JreMemoryLeakPreventionListener.*$\)@<!--\1-->@' "$CATALINA_HOME"/conf/server.xml
        sed -i -e 's@\(^[^<].*org.apache.catalina.mbeans.GlobalResourcesLifecycleListener.*$\)@<!--\1-->@' "$CATALINA_HOME"/conf/server.xml
    fi

    if ! grep '<role rolename="manager-gui"/>' "$CATALINA_HOME"/conf/tomcat-users.xml; then
        sed -i -e 's@</tomcat-users>@<role rolename="manager-gui"/>\
    </tomcat-users>@g' "$CATALINA_HOME"/conf/tomcat-users.xml
    fi
    if ! grep '<role rolename="manager-script"/>' "$CATALINA_HOME"/conf/tomcat-users.xml; then
        sed -i -e 's@</tomcat-users>@<role rolename="manager-script"/>\
    </tomcat-users>@g' "$CATALINA_HOME"/conf/tomcat-users.xml
    fi
    if ! grep '<user username="tomcat" password="tomcat" roles="tomcat,manager-gui,manager-script"/>' "$CATALINA_HOME"/conf/tomcat-users.xml; then
        sed -i -e 's@</tomcat-users>@<user username="tomcat" password="tomcat" roles="tomcat,manager-gui,manager-script"/>\
    </tomcat-users>@g' "$CATALINA_HOME"/conf/tomcat-users.xml
    fi

    export CATALINA_OPTS="-Xmx$MAX_HEAP -Djersey.config.test.memleak.tomcat.magicRunnerIdentifier -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$DIST_DIR -XX:GCTimeLimit=20 -XX:GCHeapFreeLimit=30 $JVM_ARGS"

    chmod +x "$CATALINA_HOME"/bin/startup.sh

    "$CATALINA_HOME"/bin/startup.sh

    for A in `seq $TRIES_COUNT`; do
        set +e
        nc -z localhost $PORT && break
        set -e
        sleep 5
    done
}

deploy_tomcat() {
    all_proxy="" http_proxy="" curl -sS --upload-file "$WAR_PATH" "http://tomcat:tomcat@localhost:$PORT/manager/text/deploy?path=/$CONTEXT_ROOT"
}

if [ "$SKIP_START_STOP" = "true" ]; then
    echo Start skipped
else
    start_tomcat
fi

if [ "$SKIP_DEPLOY" = "true" ]; then
    echo Deployment skipped
else
    deploy_tomcat
fi

