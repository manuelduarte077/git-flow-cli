[CmdletBinding()]
param(
  [string]$Version = "2.0.1",
  [string]$Repo = "manuelduarte077/git-flow-cli"
)

$ErrorActionPreference = "Stop"

$asset = "git-flow-cli-$Version.zip"
$url = "https://github.com/$Repo/releases/download/v$Version/$asset"
$root = Join-Path $env:LOCALAPPDATA "Programs\git-flow-cli"
$dest = Join-Path $root "git-flow-cli-$Version"
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

if (-not (Test-Path (Join-Path $binDir "git-flow-cli.bat"))) {
  throw "No se encontró git-flow-cli.bat en $binDir. Comprueba la versión y el nombre del ZIP en el release."
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
Write-Host "Instalación lista. Abre una nueva ventana de PowerShell y ejecuta: git-flow-cli --help"
