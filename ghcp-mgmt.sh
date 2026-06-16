#!/bin/sh
# GitHub Management CLI - macOS/Linux launcher
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -Dghcp-mgmt.home="$DIR" -jar "$DIR/target/ghcp-mgmt-1.0.0.jar" "$@"
