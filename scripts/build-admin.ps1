$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)

$connector = Join-Path (Get-Location) 'lib\mysql-connector-j-9.6.0.jar'
$classes = Join-Path (Get-Location) 'build\classes'
if (-not (Test-Path $connector)) { throw "Missing MySQL JDBC driver: $connector" }
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = Get-ChildItem 'src\*.java' | Where-Object { $_.Name -ne 'ViewTimetableServlet.java' } | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed; confirm that a JDK (not only a JRE) is installed and on PATH.' }
Write-Host 'Admin application compiled to build\classes.'
