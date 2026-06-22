#!/bin/sh
# GitHub Management CLI - macOS/Linux launcher
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$DIR/target/ghcp-mgmt-1.0.0.jar" ]; then
    JAR="$DIR/target/ghcp-mgmt-1.0.0.jar"
else
    JAR="$DIR/ghcp-mgmt-1.0.0.jar"
fi
exec java -Dghcp-mgmt.home="$DIR" -jar "$JAR" "$@"
