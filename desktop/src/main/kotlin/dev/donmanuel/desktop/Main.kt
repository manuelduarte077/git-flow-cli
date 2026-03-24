package dev.donmanuel.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.config.ExampleTomlTemplate
import dev.donmanuel.desktop.components.MainShell
import dev.donmanuel.desktop.components.PendingTomlDialog
import dev.donmanuel.desktop.components.ProjectSelectScreen
import dev.donmanuel.desktop.components.tomlUiStatus
import dev.donmanuel.desktop.storage.ProjectHistoryStore
import dev.donmanuel.desktop.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import kotlin.io.path.isRegularFile

fun main() = application {
    val windowState = rememberWindowState(width = 1024.dp, height = 760.dp)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Git BN Flow",
    ) {
        AppTheme {
            DesktopApp()
        }
    }
}

private sealed class ProjectFlow {
    data object Selection : ProjectFlow()
    data class PendingToml(val root: Path) : ProjectFlow()
    data class Main(val root: Path, val bnConfig: BnConfig?) : ProjectFlow()
}

@Composable
fun DesktopApp() {
    var flow by remember { mutableStateOf<ProjectFlow>(ProjectFlow.Selection) }
    var selectionError by remember { mutableStateOf<String?>(null) }
    var recentProjects by remember { mutableStateOf(ProjectHistoryStore.load()) }
    val scope = rememberCoroutineScope()

    fun openGitRoot(gitRoot: Path) {
        scope.launch(Dispatchers.IO) {
            val configPath = gitRoot.resolve(BnConfig.FILE_NAME)
            when {
                configPath.isRegularFile() -> {
                    try {
                        val cfg = BnConfig.load(configPath)
                        ProjectHistoryStore.add(gitRoot)
                        withContext(Dispatchers.Main) {
                            recentProjects = ProjectHistoryStore.load()
                            flow = ProjectFlow.Main(gitRoot, cfg)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            selectionError =
                                "No se pudo leer ${BnConfig.FILE_NAME}: ${e.message ?: e.javaClass.simpleName}"
                        }
                    }
                }

                else -> {
                    withContext(Dispatchers.Main) {
                        flow = ProjectFlow.PendingToml(gitRoot)
                    }
                }
            }
        }
    }

    when (val f = flow) {
        is ProjectFlow.Selection -> {
            ProjectSelectScreen(
                errorMessage = selectionError,
                onDismissError = { selectionError = null },
                recentProjects = recentProjects,
                onChooseFolder = {
                    selectionError = null
                    scope.launch(Dispatchers.IO) {
                        val picked = pickDirectoryBlocking()
                        if (picked == null) return@launch
                        val gitRoot = ConfigFinder.findGitRoot(picked)
                        if (gitRoot == null) {
                            selectionError = "La carpeta no está dentro de un repositorio Git (.git)."
                            return@launch
                        }
                        openGitRoot(gitRoot)
                    }
                },
                onOpenRecent = { root ->
                    selectionError = null
                    if (!Files.isDirectory(root.resolve(".git"))) {
                        ProjectHistoryStore.remove(root)
                        recentProjects = ProjectHistoryStore.load()
                        selectionError = "El proyecto ya no existe o no es un repo Git. Se quitó del historial."
                        return@ProjectSelectScreen
                    }
                    openGitRoot(root)
                },
                onRemoveFromHistory = { root ->
                    ProjectHistoryStore.remove(root)
                    recentProjects = ProjectHistoryStore.load()
                },
            )
        }

        is ProjectFlow.PendingToml -> {
            val goMainWithoutConfig = {
                ProjectHistoryStore.add(f.root)
                recentProjects = ProjectHistoryStore.load()
                flow = ProjectFlow.Main(f.root, null)
            }
            PendingTomlDialog(
                onDismissRequest = goMainWithoutConfig,
                onCreate = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val text = ExampleTomlTemplate.loadText()
                            Files.writeString(f.root.resolve(BnConfig.FILE_NAME), text)
                            val cfg = BnConfig.load(f.root.resolve(BnConfig.FILE_NAME))
                            ProjectHistoryStore.add(f.root)
                            withContext(Dispatchers.Main) {
                                recentProjects = ProjectHistoryStore.load()
                                flow = ProjectFlow.Main(f.root, cfg)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                selectionError = e.message ?: e.javaClass.simpleName
                                flow = ProjectFlow.Selection
                            }
                        }
                    }
                },
                onOmit = goMainWithoutConfig,
            )
        }

        is ProjectFlow.Main -> {
            MainShell(
                projectRoot = f.root,
                bnConfig = f.bnConfig,
                tomlStatus = tomlUiStatus(f.root),
                onChangeProject = {
                    recentProjects = ProjectHistoryStore.load()
                    flow = ProjectFlow.Selection
                },
            )
        }
    }
}

private fun pickDirectoryBlocking(): Path? {
    val holder = arrayOf<Path?>(null)
    try {
        SwingUtilities.invokeAndWait {
            val fc = JFileChooser()
            fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fc.dialogTitle = "Elegir carpeta del repositorio Git"
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                holder[0] = fc.selectedFile.toPath().toAbsolutePath().normalize()
            }
        }
    } catch (_: Exception) {
        return null
    }
    return holder[0]
}
