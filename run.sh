#!/bin/bash
LIB=lib
CP="classes:$LIB/tui4j-0.3.3.jar:$LIB/jline-terminal-jni-3.26.1.jar:$LIB/jline-native-3.26.1.jar:$LIB/jline-terminal-3.26.1.jar:$LIB/icu4j-76.1.jar:$LIB/commons-text-1.15.0.jar:$LIB/commons-lang3-3.20.0.jar"
java -cp "$CP" com.splendor.Main "$@"