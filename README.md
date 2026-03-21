# git-bn-cli

CLI (Kotlin + Clikt) para **ramas** y **commits** con el formato de trabajo BN / canales digitales.

Comando: **`git-bn-cli`**

**Repositorio:** [manuelduarte077/git-flow-cli](https://github.com/manuelduarte077/git-flow-cli) — [**Releases**](https://github.com/manuelduarte077/git-flow-cli/releases).

## Requisitos

- **Java 21+** en el PATH.

## Uso rápido

### Configuración por proyecto

En la raíz del repositorio git, crea **`.git-bn-cli.toml`** (puedes partir de [`git-bn-cli.example.toml`](git-bn-cli.example.toml)):

```toml
canal = "canales_digitales"
subcanal = "canales_2"
empresa = "NOVACOMP"
```

### Crear una rama (sin commit)

Formato **release:** `release/<siglas>_<sprint>`  
Formato **feature / hotfix:** `<tipo>/<siglas>_<sprint>_<area>_<empresa>_<refHU>`

```bash
# Release
git-bn-cli rama --tipo release --app BNMP --sprint V58-Sprint22.05

# Feature
git-bn-cli rama --tipo feature --app BNMP --sprint V58-Sprint22.05 --area DCSTI --empresa BABEL --hu HU-116268
```

### Commit con formato pipe

Tras `git add`, el mensaje queda como:

`canal|subcanal|empresa|ticket| descripción`

```bash
git-bn-cli cc --ticket "BUG 886814" -m "Implementación de validación de dispositivo"
git-bn-cli cc -t "HU-116268" -m "Descripción del cambio"
```

Solo mostrar el mensaje sin hacer commit: `git-bn-cli cc ... --print`.

Puedes sobreescribir canal/empresa con `--canal`, `--subcanal`, `--empresa` si no hay `.git-bn-cli.toml`.

### Hook `commit-msg` (validación en el repo)

Instala el hook para que **git rechace** commits cuyo mensaje no cumpla el formato pipe:

```bash
git-bn-cli hooks install
```

Comprueba un archivo de mensaje (uso interno / pruebas):

```bash
git-bn-cli hooks verify --file /ruta/al/archivo
```

Los mensajes que empiezan por `Merge ` se omiten en la validación.

## Instalar desde GitHub

Para Windows puedes usar el script vía `raw.githubusercontent.com`. Para Homebrew en macOS usa `brew tap` (ver abajo).

### macOS — Homebrew

**Homebrew 5** requiere un **tap** con la fórmula en `Formula/`. La fórmula está en [`Formula/git-bn-cli.rb`](Formula/git-bn-cli.rb).

```bash
brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
brew install git-bn-cli
```

El repositorio debe ser **público** (o el remoto del tap accesible con tus credenciales).

Desarrollo local:

```bash
brew tap manuelduarte077/git-flow-cli /ruta/al/clon/git-flow-cli
brew install git-bn-cli
```

Si aparece `UnsupportedClassVersionError`, revisa `JAVA_HOME` (debe ser Java 21, p. ej. `brew --prefix openjdk@21`).

### Windows — PowerShell

Descarga el script desde `main` y ajusta `-Version` al release que uses:

```powershell
$script = "$env:TEMP\install-git-bn-cli.ps1"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/manuelduarte077/git-flow-cli/main/packaging/install.ps1" -OutFile $script -UseBasicParsing
powershell -ExecutionPolicy Bypass -File $script -Version 2.0.0
```

### Instalación manual

Descarga el `.zip` / `.tgz` desde [Releases](https://github.com/manuelduarte077/git-flow-cli/releases), descomprime y añade `git-bn-cli-<versión>/bin` al `PATH`.

## Desarrollo

```bash
./gradlew run --args="--help"
./gradlew test
```

## Publicar una versión (mantenedores)

El workflow **Release** se dispara con **push de tag** `v*`. Ver [`.github/workflows/release.yml`](.github/workflows/release.yml).

1. Sube `version` en `build.gradle.kts` si hace falta.
2. `git tag v2.0.0 && git push origin v2.0.0`
3. Actualiza `url` / `sha256` en [`Formula/git-bn-cli.rb`](Formula/git-bn-cli.rb) para el `.tgz` del release.
