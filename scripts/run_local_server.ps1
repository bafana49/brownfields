Set-StrictMode -Version Latest
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location "$scriptDir\.."
Write-Host 'Starting local server...'
mvn -q -Dexec.mainClass=za.co.wethinkcode.robots.server.MultiServers -Dexec.cleanupDaemonThreads=false org.codehaus.mojo:exec-maven-plugin:3.6.3:java
