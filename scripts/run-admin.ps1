$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
& (Join-Path $PSScriptRoot 'build-admin.ps1')
if ($LASTEXITCODE -ne 0) { throw 'Admin build failed.' }
$connector = Join-Path (Get-Location) 'lib\mysql-connector-j-9.6.0.jar'
& java -cp "build\classes;$connector" Main
