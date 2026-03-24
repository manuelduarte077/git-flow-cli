# git-flow-cli

CLI (Kotlin + Clikt) para **ramas** y **commits** con formato BN / canales digitales.

## Requisitos

- **Java 21+** en el `PATH`.

### Comandos rápidos

| Acción | Comando |
| ------ | ------- |
| Crear rama BN | `git-flow-cli rama …` |
| Commit con formato pipe | `git-flow-cli cc -t TICKET -m "descripción"` |
| Instalar hook `commit-msg` | `git-flow-cli hooks install` |
| Ayuda | `git-flow-cli --help` · `git-flow-cli rama --help` |

## Uso

### Configuración por proyecto

En la raíz del repo Git, crea **`.git-flow-cli.toml`** (plantilla: [`git-flow-cli.example.toml`](git-flow-cli.example.toml)). También se reconoce **`.git-bn-cli.toml`** (legado).

Por defecto, **canal** y **empresa** en el mensaje de commit son `canales_digitales` y `NOVACOMP`. En `.git-flow-cli.toml` solo es obligatorio **subcanal**; puedes opcionalmente definir `canal` y `empresa` para sobrescribir esos valores en `git-flow-cli cc`.

```toml
subcanal = "canales_2"
# opcional: canal = "canales_digitales"
# opcional: empresa = "NOVACOMP"
```

### Ramas (sin commit)


| Tipo             | Formato de nombre                                   |
| ---------------- | --------------------------------------------------- |
| Release          | `release/<siglas>_<sprint>`                         |
| Feature / hotfix | `<tipo>/<siglas>_<sprint>_<area>_<empresa>_<refHU>` |

En feature/hotfix el segmento de **empresa** en el nombre de rama es siempre **NOVACOMP** (no hace falta `--empresa`).

```bash
git-flow-cli rama --tipo release --app BNMP --sprint V58-Sprint22.05
git-flow-cli rama --tipo feature --app BNMP --sprint V58-Sprint22.05 --area DCSTI --hu HU-116268
```

Sin flags (o faltando `--tipo`, `--app` o `--sprint`), **`rama` entra en modo interactivo**: pregunta por consola y muestra cómo quedará el nombre de la rama antes de crearla.

### Commits con formato pipe

El mensaje es `canal|subcanal|empresa|ticket| descripción` (por defecto `canales_digitales` y `NOVACOMP` salvo TOML). **Antes del commit** debe haber cambios en staging (`git add`).

```bash
git add archivo1 archivo2   # o git add .
git-flow-cli cc --ticket "BUG 886814" -m "Implementación de validación de dispositivo"
git-flow-cli cc -t "HU-116268" -m "Descripción del cambio"
```

Sin `--ticket` / `--m` (o faltando uno), **`cc` entra en modo interactivo**. El **subcanal** sale del TOML, de `--subcanal` o del prompt si falta.

```bash
git-flow-cli cc --print   # solo imprime el mensaje pipe, no ejecuta git commit
```

### Hook `commit-msg`

Valida el formato en cada commit (se ignoran líneas comentario `#` y mensajes que empiezan por `Merge `).

La instalación usa el directorio real de hooks (`git rev-parse --git-path hooks`), así que respeta **`core.hooksPath`** y worktrees. Si ya existe un `commit-msg` que no sea de git-flow-cli, se guarda como `commit-msg.bak` (salvo `--force`).

```bash
git-flow-cli hooks install
git-flow-cli hooks install --resolve-binary   # ruta absoluta al ejecutable (p. ej. IDE sin git-flow-cli en PATH)
git-flow-cli hooks install --force              # sobrescribe sin copia de seguridad
```

`hooks verify` solo acepta archivos **dentro del directorio Git** del repositorio (p. ej. `.git/COMMIT_EDITMSG`). Uso habitual: lo invoca Git al hacer commit; no hace falta llamarlo a mano salvo pruebas.

```bash
git-flow-cli hooks verify --file .git/COMMIT_EDITMSG
```

### Solución de problemas

| Problema | Qué hacer |
| -------- | --------- |
| `git-flow-cli: ejecutable no encontrado en PATH` al hacer commit | Asegúrate de que `git-flow-cli` está en el `PATH` del mismo entorno que abre Git (tras instalar, **cierra y vuelve a abrir** la terminal o el IDE). En Windows, abre una terminal nueva tras el instalador PowerShell. Prueba `hooks install --resolve-binary`. |
| El hook no se ejecuta | Comprueba que instalaste en el repo correcto y que `git rev-parse --git-path hooks` apunta al directorio donde está el script. |
| `cc` dice que no hay cambios en staging | Ejecuta `git add` (o `git add .`) antes de `git-flow-cli cc`. |
| `hooks verify` rechaza una ruta | Debe ser un archivo bajo el directorio Git del repo (p. ej. no un fichero temporal fuera de `.git`). |
| Formato de mensaje rechazado | Debe haber exactamente cinco segmentos separados por `\|` y una descripción no vacía: `canal\|subcanal\|empresa\|ticket\| descripción`. |

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

Si más tarde quieres el tap desde GitHub y ves `**remote mismatch**` (el tap sigue apuntando a tu clon local), alinea el origen o recrea el tap:

```bash
brew uninstall git-flow-cli 2>/dev/null || true
brew untap manuelduarte077/git-flow-cli
brew tap manuelduarte077/git-flow-cli https://github.com/manuelduarte077/git-flow-cli
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

Tras instalar, abre una **nueva** ventana de PowerShell (o reinicia el IDE) para que el `PATH` incluya `git-flow-cli`; el hook `commit-msg` usa el mismo entorno que Git.

```powershell
$script = "$env:TEMP\install-git-flow-cli.ps1"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/manuelduarte077/git-flow-cli/main/packaging/install.ps1" -OutFile $script -UseBasicParsing
powershell -ExecutionPolicy Bypass -File $script -Version 2.0.2
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
2. `git tag v2.0.2 && git push origin v2.0.2`
3. Tras el release, actualiza `url` y `sha256` en `[Formula/git-flow-cli.rb](Formula/git-flow-cli.rb)` según el `.tgz` publicado (el SHA de CI puede no coincidir con un build local).

