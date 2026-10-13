param(
    [Parameter(Mandatory = $true)]
    [string]$TomcatHome
)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$tomcat = (Resolve-Path $TomcatHome).Path
$servletApi = Join-Path $tomcat 'lib\servlet-api.jar'
$connector = Join-Path (Get-Location) 'lib\mysql-connector-j-9.6.0.jar'
if (-not (Test-Path $servletApi)) { throw "Tomcat servlet API not found: $servletApi (use Tomcat 9)." }
if (-not (Test-Path $connector)) { throw "Missing MySQL JDBC driver: $connector" }

$stage = Join-Path (Get-Location) 'build\webapp'
$classes = Join-Path $stage 'WEB-INF\classes'
$libs = Join-Path $stage 'WEB-INF\lib'
if (Test-Path $stage) { Remove-Item -Recurse -Force $stage }
New-Item -ItemType Directory -Force -Path $stage | Out-Null
Copy-Item 'web\*' $stage -Recurse -Force
New-Item -ItemType Directory -Force -Path $classes, $libs | Out-Null
$sources = Get-ChildItem 'src\*.java' | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -cp "$connector;$servletApi" -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'Web application compile failed.' }
Copy-Item $connector $libs
$war = Join-Path (Get-Location) 'build\examcell.war'
if (Test-Path $war) { Remove-Item -Force $war }
Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $war
Copy-Item $war (Join-Path $tomcat 'webapps\examcell.war') -Force
Write-Host "Deployed $war to Tomcat. Start Tomcat and open http://localhost:8080/examcell/timetable"
