#!/bin/bash

#put together classpath from eery file in bin

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR/..

export CLASSPATH=conf:lib/*
java -cp $CLASSPATH com.alvazan.tcpproxy.Main
