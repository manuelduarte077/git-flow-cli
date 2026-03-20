# git-bn-cli

CLI en Kotlin para crear ramas y commits con la convención de tu flujo (Clikt + Git).

Comando instalado: **`git-bn-cli`**

**Repositorio en GitHub:** [manuelduarte077/git-flow-cli](https://github.com/manuelduarte077/git-flow-cli) — los binarios publicados están en [**Releases**](https://github.com/manuelduarte077/git-flow-cli/releases).

## Requisitos

- **Java 21+** en el PATH (Temurin, Azul, Homebrew `openjdk@21`, etc.).

---

## Instalar desde GitHub

Para Windows puedes usar el script vía `raw.githubusercontent.com`. Para Homebrew en macOS usa `brew tap` (ver abajo).

### macOS — Homebrew (fórmula apuntando al release en GitHub)

**Homebrew 5** ya no instala desde `brew install https://raw.githubusercontent.com/.../formula.rb`: interpreta el nombre del `.rb` y busca `git-bn-cli` en los taps (de ahí el aviso *No available formula*). Tampoco acepta `brew install ./ruta/formula.rb` si el archivo no forma parte de un tap.

La fórmula está en [`Formula/git-bn-cli.rb`](Formula/git-bn-cli.rb). Instalación recomendada: **añadir el repo como tap** y luego instalar:

```bash
brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
brew install git-bn-cli
```

El repositorio debe ser **público** (o el `tap` con URL debe ser accesible con tus credenciales git).

Si desarrollas en local:

```bash
brew tap manuelduarte077/git-flow-cli /ruta/al/clon/git-flow-cli
brew install git-bn-cli
```

La fórmula usa `url` y `sha256` del artefacto `git-bn-cli-<versión>.tgz` publicado en Releases. Si falla la comprobación de integridad, actualiza el `sha256` en [`Formula/git-bn-cli.rb`](Formula/git-bn-cli.rb).

### Windows — PowerShell (script desde GitHub)

Descarga el script de instalación desde la rama `main` y ejecútalo (elige la misma **versión** que el [release](https://github.com/manuelduarte077/git-flow-cli) que quieras, por defecto `1.0.1`):

```powershell
$script = "$env:TEMP\install-git-bn-cli.ps1"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/manuelduarte077/git-flow-cli/main/packaging/install.ps1" -OutFile $script -UseBasicParsing
powershell -ExecutionPolicy Bypass -File $script -Version 1.0.1
```

Otra versión u otro fork:

```powershell
powershell -ExecutionPolicy Bypass -File $script -Version 1.0.1 -Repo "owner/repo"
```

El script descarga el **ZIP** del release en GitHub, lo instala en `%LOCALAPPDATA%\Programs\git-bn-cli\` y añade `bin` al PATH del usuario.

### Instalación manual (cualquier SO)

1. Abre [**Releases**](https://github.com/manuelduarte077/git-flow-cli/releases).

2. En la versión deseada, descarga **`git-bn-cli-<versión>.zip`** (Windows) o **`.tgz`** (macOS/Linux).

3. Descomprime y añade la carpeta `git-bn-cli-<versión>/bin` al `PATH` del sistema.

4. Comprueba: `git-bn-cli --help`.

---


## Desarrollo

```bash
./gradlew run --args="--help"
```

## Publicar una versión (mantenedores)

El workflow **Release** solo se dispara al **push de un tag** `v*` (no al merge a `main`). Ver [`.github/workflows/release.yml`](.github/workflows/release.yml).

1. Ajusta `version` en `build.gradle.kts` si hace falta.
2. `git tag v1.0.1 && git push origin v1.0.1`
3. Tras el workflow, actualiza `url`, `sha256` en [`Formula/git-bn-cli.rb`](Formula/git-bn-cli.rb) si cambia el artefacto.

**Opcional:** **Actions → Release → Run workflow** para repetir el build.
