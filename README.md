# git-flow-cli

CLI (Kotlin + Clikt) para **ramas** y **commits** con formato BN / canales digitales.

El repositorio es multi-módulo Gradle: `**:core`** (validadores, Git, TOML), `**:cli**` (Clikt, binario `git-flow-cli`) y `**:desktop**` (Compose Desktop opcional).

## Requisitos

- **Java 21** (JDK 21) en el `PATH` o vía toolchain de Gradle (Foojay).

### Comandos rápidos


| Acción                                | Comando                                                                                         |
| ------------------------------------- | ----------------------------------------------------------------------------------------------- |
| Crear rama BN                         | `git-flow-cli rama …`                                                                           |
| Validar nombre de rama (BN)           | `git-flow-cli rama verify` · `git-flow-cli rama verify --name hotfix/...`                       |
| Commit con formato pipe               | `git-flow-cli cc -t TICKET -m "descripción"`                                                    |
| Instalar hook `commit-msg`            | `git-flow-cli hooks install`                                                                    |
| Validar rama al hacer checkout        | `git-flow-cli hooks install --branch-hook`                                                      |
| Diagnóstico (Java, git, TOML, hooks)  | `git-flow-cli doctor`                                                                           |
| Crear `.git-flow-cli.toml` de ejemplo | `git-flow-cli init` (en la raíz del repo)                                                       |
| Autocompletado bash/zsh               | `git-flow-cli generate-completion bash` o `zsh` → guardar y `source` (ver ayuda del subcomando) |
| Ayuda                                 | `git-flow-cli --help` · `git-flow-cli rama --help`                                              |


## Uso

### Configuración por proyecto

En la raíz del repo Git, crea `**.git-flow-cli.toml`** (plantilla: `[git-flow-cli.example.toml](git-flow-cli.example.toml)`). También se reconoce `**.git-bn-cli.toml**` (legado).

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

Sin flags (o faltando `--tipo`, `--app` o `--sprint`), `**rama` entra en modo interactivo**: pregunta por consola y muestra cómo quedará el nombre de la rama antes de crearla.

Si creas la rama **a mano** con `git checkout -b …`, comprueba el nombre con:

```bash
git-flow-cli rama verify              # rama actual
git-flow-cli rama verify --name hotfix/BNMP_V58-Sprint22.05_DCSTI_NOVACOMP_HU-116268
```

Para validar **automáticamente** tras cada cambio de rama (incluido `checkout -b`), instala el hook opcional:

```bash
git-flow-cli hooks install --branch-hook
```

(Se añade `post-checkout`; solo actúa en checkouts de rama, no en cambios de archivos. Ramas como `main` o nombres sin prefijo `feature/`/`hotfix/`/`release/` no se rechazan.)

### Commits con formato pipe

El mensaje es `canal|subcanal|empresa|ticket| descripción` (por defecto `canales_digitales` y `NOVACOMP` salvo TOML). **Antes del commit** debe haber cambios en staging (`git add`).

```bash
git add archivo1 archivo2   # o git add .
git-flow-cli cc --ticket "BUG 886814" -m "Implementación de validación de dispositivo"
git-flow-cli cc -t "HU-116268" -m "Descripción del cambio"
```

Sin `--ticket` / `--m` (o faltando uno), `**cc` entra en modo interactivo**. El **subcanal** sale del TOML, de `--subcanal` o del prompt si falta.

```bash
git-flow-cli cc --print   # solo imprime el mensaje pipe, no ejecuta git commit
```

### Hook `commit-msg`

Valida el formato en cada commit (se ignoran líneas comentario `#` y mensajes que empiezan por `Merge` ).

La instalación usa el directorio real de hooks (`git rev-parse --git-path hooks`), así que respeta `**core.hooksPath**` y worktrees. Si ya existe un `commit-msg` que no sea de git-flow-cli, se guarda como `commit-msg.bak` (salvo `--force`).

```bash
git-flow-cli hooks install
git-flow-cli hooks install --resolve-binary   # ruta absoluta al ejecutable (p. ej. IDE sin git-flow-cli en PATH)
git-flow-cli hooks install --force              # sobrescribe sin copia de seguridad
git-flow-cli hooks install --branch-hook        # además: post-checkout para validar nombre BN de rama
```

`hooks verify` solo acepta archivos **dentro del directorio Git** del repositorio (p. ej. `.git/COMMIT_EDITMSG`). Uso habitual: lo invoca Git al hacer commit; no hace falta llamarlo a mano salvo pruebas.

```bash
git-flow-cli hooks verify --file .git/COMMIT_EDITMSG
```

### Solución de problemas


| Problema                                                                  | Qué hacer                                                                                                                                                                                                                                                                  |
| ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `git-flow-cli: ejecutable no encontrado en PATH` al hacer commit          | Asegúrate de que `git-flow-cli` está en el `PATH` del mismo entorno que abre Git (tras instalar, **cierra y vuelve a abrir** la terminal o el IDE). En Windows, abre una terminal nueva tras el instalador PowerShell. Prueba `hooks install --resolve-binary`.            |
| `Could not find or load main class hooks` / `ClassNotFoundException: hoo` | El hook apuntaba a `java` en lugar del script `git-flow-cli` (típico con `--resolve-binary` en versiones antiguas). Actualiza git-flow-cli y vuelve a ejecutar `git-flow-cli hooks install --resolve-binary`, o reinstala sin `--resolve-binary` para usar solo el `PATH`. |
| El hook no se ejecuta                                                     | Comprueba que instalaste en el repo correcto y que `git rev-parse --git-path hooks` apunta al directorio donde está el script.                                                                                                                                             |
| `cc` dice que no hay cambios en staging                                   | Ejecuta `git add` (o `git add .`) antes de `git-flow-cli cc`.                                                                                                                                                                                                              |
| `hooks verify` rechaza una ruta                                           | Debe ser un archivo bajo el directorio Git del repo (p. ej. no un fichero temporal fuera de `.git`).                                                                                                                                                                       |
| Formato de mensaje rechazado                                              | Debe haber exactamente cinco segmentos separados por `|` y una descripción no vacía: `canal|subcanal|empresa|ticket| descripción`.                                                                                                                                         |
| `rama verify` rechaza el nombre tras `git checkout -b`                    | El nombre debe seguir el formato BN (p. ej. hotfix con 5 segmentos y empresa `NOVACOMP`). Renombra con `git branch -m nuevo-nombre` o usa `git-flow-cli rama` para generar el nombre.                                                                                      |


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


## App de escritorio (Compose, opcional)

Ventana con pestañas **Rama**, **Commit (cc)** y **Acerca de**; usa la misma lógica que el CLI (`:core`). No sustituye al binario en PATH para hooks.

```bash
./gradlew :desktop:run
```

En desarrollo, macOS suele mostrar el proceso como **java** en el Dock (icono genérico); para ver nombre e icono del producto usa `./gradlew :desktop:runDistributable` o el `.app` tras `packageDmg`.

En macOS, la app fija por defecto **Skiko en modo SOFTWARE** (CPU) si no defines otra cosa, para evitar fallos JNI con **Metal** en algunos JDK (`UnsatisfiedLinkError`). Para forzar **Metal** (GPU): `SKIKO_RENDER_API=METAL` o `-Dskiko.renderApi=METAL`.

Con **JDK 24+**, la JVM restringe `System.load` (Skiko carga librerías nativas). El `desktop/build.gradle.kts` añade `--enable-native-access=ALL-UNNAMED` a las tareas `JavaExec` cuando la versión del **launcher** es ≥ 24. El proyecto fija **Java 21** en el `build.gradle.kts` raíz (`jvmToolchain(21)`); alinea `JAVA_HOME` con esa toolchain si ves errores.

Empaquetado nativo (según SO: DMG, MSI, DEB, etc.):

```bash
./gradlew :desktop:packageDistributionForCurrentOS
```

Los artefactos quedan bajo `desktop/build/compose/binaries/`. En macOS también puedes usar `:desktop:packageDmg`; en Windows `:desktop:packageMsi`; en Linux `:desktop:packageDeb`.

## Desarrollo

```bash
./gradlew :cli:run --args="--help"
./gradlew :core:test :cli:build
```

Distribución del CLI (ZIP/TGZ) del módulo `:cli`:

```bash
./gradlew :cli:distZip :cli:distTar
# artefactos: cli/build/distributions/
```

## Publicar releases (mantenedores)

Workflow: `[.github/workflows/release.yml](.github/workflows/release.yml)` (se dispara con tag `v*`). Construye el ZIP/TGZ del CLI, el DMG de macOS (`:desktop:packageDmg`) y el MSI de Windows (`:desktop:packageMsi`) y adjunta todo al mismo GitHub Release.

1. Ajusta `version` en el `build.gradle.kts` raíz (`allprojects { version = ... }`) si aplica.
2. `git tag v2.0.2 && git push origin v2.0.2`
3. Tras el release, actualiza `url` y `sha256` en `[Formula/git-flow-cli.rb](Formula/git-flow-cli.rb)` según el `.tgz` publicado (el SHA de CI puede no coincidir con un build local).

