# git-flow-cli

CLI (Kotlin + Clikt) para **ramas** y **commits** con formato BN / canales digitales.


|          |                                                                                 |
| -------- | ------------------------------------------------------------------------------- |
| Comando  | `git-flow-cli`                                                                  |
| Código   | [manuelduarte077/git-flow-cli](https://github.com/manuelduarte077/git-flow-cli) |
| Binarios | [Releases](https://github.com/manuelduarte077/git-flow-cli/releases)            |


## Contenido

- [Requisitos](#requisitos)
- [Uso](#uso)
- [Instalación](#instalación)
- [Versión y actualizaciones](#versión-y-actualizaciones)
- [Desarrollo](#desarrollo)
- [Publicar releases](#publicar-releases-mantenedores)

## Requisitos

- **Java 21+** en el `PATH`.

## Uso

### Configuración por proyecto

En la raíz del repo Git, crea `**.git-flow-cli.toml`** (plantilla: `[git-flow-cli.example.toml](git-flow-cli.example.toml)`). También se reconoce `**.git-bn-cli.toml**` (legado).

```toml
canal = "canales_digitales"
subcanal = "canales_2"
empresa = "NOVACOMP"
```

### Ramas (sin commit)


| Tipo             | Formato de nombre                                   |
| ---------------- | --------------------------------------------------- |
| Release          | `release/<siglas>_<sprint>`                         |
| Feature / hotfix | `<tipo>/<siglas>_<sprint>_<area>_<empresa>_<refHU>` |


```bash
git-flow-cli rama --tipo release --app BNMP --sprint V58-Sprint22.05
git-flow-cli rama --tipo feature --app BNMP --sprint V58-Sprint22.05 --area DCSTI --empresa BABEL --hu HU-116268
```

### Commits con formato pipe

Tras `git add`, el mensaje es: `canal|subcanal|empresa|ticket| descripción`

```bash
git-flow-cli cc --ticket "BUG 886814" -m "Implementación de validación de dispositivo"
git-flow-cli cc -t "HU-116268" -m "Descripción del cambio"
git-flow-cli cc ... --print   # solo imprime el mensaje, no hace commit
```

Opcionales: `--canal`, `--subcanal`, `--empresa` si no usas el TOML o quieres sobreescribir.

### Hook `commit-msg`

Valida el formato en cada commit (los mensajes que empiezan por `Merge`  se ignoran).

```bash
git-flow-cli hooks install
git-flow-cli hooks verify --file /ruta/al/archivo
```

## Instalación

### macOS (Homebrew)

Fórmula: `[Formula/git-flow-cli.rb](Formula/git-flow-cli.rb)` (compatible con Homebrew 5: carpeta `Formula/` en el tap).

```bash
brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
brew install git-flow-cli
```

Tap desde un clon local (desarrollo):

```bash
brew tap manuelduarte077/git-flow-cli /ruta/al/clon/git-flow-cli
brew install git-flow-cli
```

**Migración** desde el binario antiguo `git-bn-cli`:

```bash
brew uninstall git-bn-cli 2>/dev/null || true
brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
brew update && brew install git-flow-cli
```

`**UnsupportedClassVersionError`:** revisa `JAVA_HOME` (Java 21, p. ej. `brew --prefix openjdk@21`).

### Windows (PowerShell)

```powershell
$script = "$env:TEMP\install-git-flow-cli.ps1"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/manuelduarte077/git-flow-cli/main/packaging/install.ps1" -OutFile $script -UseBasicParsing
powershell -ExecutionPolicy Bypass -File $script -Version 2.0.1
```

### Manual

Descarga `.zip` o `.tgz` desde [Releases](https://github.com/manuelduarte077/git-flow-cli/releases), descomprime y añade `git-flow-cli-<versión>/bin` al `PATH`.

## Versión y actualizaciones


| Necesidad           | Comando / acción                                                                                                                                                                                             |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Versión instalada   | `git-flow-cli --version`, `-V`, o `git-flow-cli about`                                                                                                                                                       |
| ¿Hay release nueva? | `git-flow-cli about --check-updates` (requiere red)                                                                                                                                                          |
| Otra versión        | **Homebrew:** `brew upgrade git-flow-cli`. **ZIP:** asset `git-flow-cli-<versión>.zip` en [Releases](https://github.com/manuelduarte077/git-flow-cli/releases). **PowerShell:** `install.ps1 -Version x.y.z` |
| Actualizar          | Según método: `brew upgrade`, sustituir carpeta del ZIP en el `PATH`, o volver a ejecutar `install.ps1`                                                                                                      |


## Desarrollo

```bash
./gradlew run --args="--help"
./gradlew test
```

## Publicar releases (mantenedores)

Workflow: `[.github/workflows/release.yml](.github/workflows/release.yml)` (se dispara con tag `v*`).

1. Ajusta `version` en `build.gradle.kts` si aplica.
2. `git tag v2.0.1 && git push origin v2.0.1`
3. Tras el release, actualiza `url` y `sha256` en `[Formula/git-flow-cli.rb](Formula/git-flow-cli.rb)` según el `.tgz` publicado (el SHA de CI puede no coincidir con un build local).

