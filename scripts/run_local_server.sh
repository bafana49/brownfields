#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Pass a dummy arg so MultiServers starts without the Swing GUI (World(false)).
exec mvn -q -Dexec.mainClass=za.co.wethinkcode.robots.server.MultiServers -Dexec.args=nogui -Dexec.cleanupDaemonThreads=false org.codehaus.mojo:exec-maven-plugin:3.6.3:java
