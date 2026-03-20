<#
.SYNOPSIS
  Instala el binario git-bn-cli (Windows) desde un release de GitHub.

.DESCRIPTION
  Descarga el ZIP de la distribución Gradle, lo descomprime en
  %LOCALAPPDATA%\Programs\git-bn-cli\<version> y añade ...\bin al PATH del usuario.

.PARAMETER Version
  Versión del release (ej. 1.0.0), sin prefijo v.

.PARAMETER Repo
  Repositorio GitHub en formato owner/name.

.EXAMPLE
  .\packaging\install.ps1

.EXAMPLE
  .\packaging\install.ps1 -Version 1.0.0 -Repo "manuelduarte077/git-flow-cli"
#>
[CmdletBinding()]
param(
  [string]$Version = "1.0.0",
  [string]$Repo = "manuelduarte077/git-flow-cli"
)

$ErrorActionPreference = "Stop"

$asset = "git-bn-cli-$Version.zip"
$url = "https://github.com/$Repo/releases/download/v$Version/$asset"
$root = Join-Path $env:LOCALAPPDATA "Programs\git-bn-cli"
$dest = Join-Path $root "git-bn-cli-$Version"
$binDir = Join-Path $dest "bin"

Write-Host "Descargando $url"
$tmp = Join-Path ([System.IO.Path]::GetTempPath()) $asset
Invoke-WebRequest -Uri $url -OutFile $tmp -UseBasicParsing

if (-not (Test-Path $root)) {
  New-Item -ItemType Directory -Path $root -Force | Out-Null
}
if (Test-Path $dest) {
  Remove-Item -Path $dest -Recurse -Force
}

Write-Host "Extrayendo en $dest"
Expand-Archive -Path $tmp -DestinationPath $root -Force

if (-not (Test-Path (Join-Path $binDir "git-bn-cli.bat"))) {
  throw "No se encontró git-bn-cli.bat en $binDir. Comprueba la versión y el nombre del ZIP en el release."
}

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$binDir*") {
  $newPath = if ([string]::IsNullOrEmpty($userPath)) { $binDir } else { "$userPath;$binDir" }
  [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
  $env:Path = "$env:Path;$binDir"
  Write-Host "Añadido al PATH de usuario: $binDir"
} else {
  Write-Host "El directorio bin ya está en el PATH de usuario."
}

Write-Host ""
Write-Host "Instalación lista. Abre una nueva ventana de PowerShell y ejecuta: git-bn-cli --help"
