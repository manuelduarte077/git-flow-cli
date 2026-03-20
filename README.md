# git-bn-cli

CLI en Kotlin para crear ramas y commits con la convención de tu flujo (Clikt + Git).

Comando instalado: **`git-bn-cli`**

## Requisitos

- **Java 21+** en el PATH (Temurin, Azul, Homebrew `openjdk@21`, etc.).

## macOS — Homebrew

### Opción A: fórmula desde este repositorio

```bash
brew install --formula ./packaging/homebrew/git-bn-cli.rb
```

Antes, si instalas desde un release de GitHub, actualiza en `packaging/homebrew/git-bn-cli.rb`:

1. `url` y `version` acordes al release.
2. `sha256` con el resultado de:

```bash
shasum -a 256 git-bn-cli-1.0.1.tgz
```

(El archivo `.tgz` se genera con `./gradlew distTar` o sale en [Releases](https://github.com/manuelduarte077/git-flow-cli/releases) si usas CI.)

### Opción B: tap propio

Crea un repo `homebrew-tu-tap`, copia `packaging/homebrew/git-bn-cli.rb`, y:

```bash
brew tap TU_USUARIO/tu-tap https://github.com/TU_USUARIO/homebrew-tu-tap
brew install git-bn-cli
```

## Windows — PowerShell

Con un [release](https://github.com/manuelduarte077/git-flow-cli/releases) publicado (misma versión que en el script):

```powershell
# Desde la raíz del repo clonado:
powershell -ExecutionPolicy Bypass -File .\packaging\install.ps1

# Otro repo o versión:
powershell -ExecutionPolicy Bypass -File .\packaging\install.ps1 -Version 1.0.1 -Repo "owner/repo"
```

El script descarga el ZIP, lo instala en `%LOCALAPPDATA%\Programs\git-bn-cli\` y añade `bin` al PATH del usuario. Necesitas **JDK 21+** instalado y en el PATH.

## Desarrollo

```bash
./gradlew run --args="--help"
```

## Publicar una versión

**Por qué no corre el release al hacer solo `git push` a `main`:** el workflow [`.github/workflows/release.yml`](.github/workflows/release.yml) está definido con `on.push.tags: ["v*"]`, así que **solo** se ejecuta al **subir un tag** cuyo nombre empiece por `v` (p. ej. `v1.0.1`). Un push de commits a una rama **no** dispara `action-gh-release`.

1. Ajusta `version` en `build.gradle.kts` si hace falta.
2. Crea y sube un tag: `git tag v1.0.1 && git push origin v1.0.1` (si el tag ya existe localmente: `git push origin v1.0.1`).
3. El workflow **Release** sube `git-bn-cli-*.zip`, `git-bn-cli-*.tgz` y `SHA256SUMS`.
4. Actualiza la fórmula Homebrew con el nuevo `sha256` del `.tgz` (o el valor del asset en la página del release).

**Opcional:** en **Actions → Release → Run workflow** puedes lanzar el mismo job a mano (útil si quieres repetir un build sin mover el tag).


