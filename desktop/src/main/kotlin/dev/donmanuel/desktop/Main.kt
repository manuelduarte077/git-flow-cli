package dev.donmanuel.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.config.ExampleTomlTemplate
import dev.donmanuel.desktop.components.MainShell
import dev.donmanuel.desktop.components.MainTool
import dev.donmanuel.desktop.components.PendingTomlDialog
import dev.donmanuel.desktop.components.ProjectSelectScreen
import dev.donmanuel.desktop.components.TomlUiStatus
import dev.donmanuel.desktop.components.tomlUiStatus
import dev.donmanuel.desktop.desktopTheme.ThemeMode
import dev.donmanuel.desktop.generated.Res
import dev.donmanuel.desktop.generated.ic_bn
import dev.donmanuel.desktop.logging.DesktopLog
import dev.donmanuel.desktop.navigation.RouteMain
import dev.donmanuel.desktop.navigation.RoutePendingToml
import dev.donmanuel.desktop.navigation.RouteSelection
import dev.donmanuel.desktop.screens.AboutContent
import dev.donmanuel.desktop.screens.SettingsDialogContent
import dev.donmanuel.desktop.storage.DesktopPreferencesStore
import dev.donmanuel.desktop.storage.ProjectHistoryStore
import dev.donmanuel.desktop.theme.AppSpacing
import dev.donmanuel.desktop.theme.AppTextButton
import dev.donmanuel.desktop.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

private fun applyDefaultSkikoRenderApiOnMacOs() {
    val os = System.getProperty("os.name")?.lowercase() ?: return
    if (!os.contains("mac")) return
    if (System.getProperty("skiko.renderApi") != null) return
    if (!System.getenv("SKIKO_RENDER_API").isNullOrBlank()) return
    System.setProperty("skiko.renderApi", "SOFTWARE")
}

private fun resolveDark(themeMode: ThemeMode, systemDark: Boolean): Boolean =
    when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    applyDefaultSkikoRenderApiOnMacOs()
    application {
        val windowState = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
        val windowIcon = painterResource(Res.drawable.ic_bn)
        var themeMode by remember { mutableStateOf(DesktopPreferencesStore.loadTheme()) }
        var ccPrefs by remember { mutableStateOf(DesktopPreferencesStore.loadCcPrefs()) }
        val menuBindingsState = remember { mutableStateOf(AppMenuBindings()) }
        val systemDark = isSystemInDarkTheme()
        val darkTheme = resolveDark(themeMode, systemDark)

        fun applyTheme(mode: ThemeMode) {
            themeMode = mode
            DesktopPreferencesStore.saveTheme(mode)
        }

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Git BN Flow",
            icon = windowIcon,
            onKeyEvent = { event ->
                if (event.type != KeyEventType.KeyDown) return@Window false
                if (!event.isMetaPressed) return@Window false
                val b = menuBindingsState.value
                when (event.key) {
                    Key.O -> {
                        b.openProject()
                        true
                    }
                    Key.Comma -> {
                        b.showSettings()
                        true
                    }
                    Key.One -> {
                        val fn = b.selectRama
                        if (fn != null) {
                            fn()
                            true
                        } else {
                            false
                        }
                    }
                    Key.Two -> {
                        val fn = b.selectCc
                        if (fn != null) {
                            fn()
                            true
                        } else {
                            false
                        }
                    }
                    Key.I -> {
                        b.showAbout()
                        true
                    }
                    else -> false
                }
            },
        ) {
            val density = LocalDensity.current
            SideEffect {
                with(density) {
                    window.minimumSize = Dimension(900.dp.roundToPx(), 600.dp.roundToPx())
                }
            }
            MenuBar {
                Menu("Archivo", mnemonic = 'A') {
                    Item(
                        "Abrir proyecto…",
                        onClick = { menuBindingsState.value.openProject() },
                        shortcut = KeyShortcut(Key.O, meta = true),
                    )
                    Item(
                        "Ajustes…",
                        onClick = { menuBindingsState.value.showSettings() },
                        shortcut = KeyShortcut(Key.Comma, meta = true),
                    )
                    Separator()
                    Item(
                        "Salir",
                        onClick = ::exitApplication,
                        shortcut = KeyShortcut(Key.Q, meta = true),
                    )
                }
                Menu("Vista", mnemonic = 'V') {
                    Item(
                        "Tema: usar sistema",
                        onClick = { menuBindingsState.value.setTheme(ThemeMode.SYSTEM) },
                    )
                    Item(
                        "Tema: claro",
                        onClick = { menuBindingsState.value.setTheme(ThemeMode.LIGHT) },
                    )
                    Item(
                        "Tema: oscuro",
                        onClick = { menuBindingsState.value.setTheme(ThemeMode.DARK) },
                    )
                }
                Menu("Ayuda", mnemonic = 'Y') {
                    Item(
                        "Acerca de Git BN Flow",
                        onClick = { menuBindingsState.value.showAbout() },
                        shortcut = KeyShortcut(Key.I, meta = true),
                    )
                }
            }
            AppTheme(darkTheme = darkTheme) {
                DesktopApp(
                    menuBindingsState = menuBindingsState,
                    ccPrefs = ccPrefs,
                    onCcPrefsChange = { ccPrefs = it },
                    themeMode = themeMode,
                    onThemeSelected = ::applyTheme,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopApp(
    menuBindingsState: MutableState<AppMenuBindings>,
    ccPrefs: DesktopPreferencesStore.CcPrefs,
    onCcPrefsChange: (DesktopPreferencesStore.CcPrefs) -> Unit,
    themeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    val navController = rememberNavController()
    var selectionError by remember { mutableStateOf<String?>(null) }
    var recentProjects by remember { mutableStateOf(ProjectHistoryStore.load()) }
    var aboutOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var pendingFolderPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    var mainToolByRoot by remember { mutableStateOf(mapOf<String, MainTool?>()) }

    fun navigateToSelectionClearingStack() {
        navController.navigate(RouteSelection) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun openGitRoot(gitRoot: Path) {
        scope.launch(Dispatchers.IO) {
            val configPath = gitRoot.resolve(BnConfig.FILE_NAME)
            when {
                configPath.isRegularFile() -> {
                    ProjectHistoryStore.add(gitRoot)
                    withContext(Dispatchers.Main) {
                        recentProjects = ProjectHistoryStore.load()
                        selectionError = null
                        navController.navigate(RouteMain(gitRoot.toString())) {
                            popUpTo<RouteSelection> { inclusive = true }
                        }
                    }
                }

                else -> {
                    withContext(Dispatchers.Main) {
                        selectionError = null
                        navController.navigate(RoutePendingToml(gitRoot.toString())) {
                            popUpTo<RouteSelection> { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    val folderPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single,
    ) { files ->
        val path = files.firstOrNull()?.file?.toPath()?.toAbsolutePath()?.normalize()
            ?: return@rememberFilePickerLauncher
        selectionError = null
        scope.launch(Dispatchers.IO) {
            val gitRoot = ConfigFinder.findGitRoot(path)
            if (gitRoot == null) {
                withContext(Dispatchers.Main) {
                    selectionError = "La carpeta no está dentro de un repositorio Git (.git)."
                }
                return@launch
            }
            openGitRoot(gitRoot)
        }
    }

    fun openProjectAction() {
        val dest = navController.currentBackStackEntry?.destination
        when {
            dest?.hasRoute(RouteMain::class) == true -> {
                navigateToSelectionClearingStack()
                pendingFolderPicker = true
            }
            dest?.hasRoute(RoutePendingToml::class) == true -> {
                navigateToSelectionClearingStack()
                pendingFolderPicker = true
            }
            else -> folderPicker.launch()
        }
    }

    val routeMainKey =
        if (backStackEntry?.destination?.hasRoute(RouteMain::class) == true) {
            backStackEntry?.toRoute<RouteMain>()?.root
        } else {
            null
        }
    val currentMainTool = routeMainKey?.let { mainToolByRoot[it] }

    LaunchedEffect(
        backStackEntry?.destination?.route,
        routeMainKey,
        currentMainTool,
        themeMode,
        onThemeSelected,
    ) {
        val selectRama: (() -> Unit)? =
            routeMainKey?.let { key ->
                { mainToolByRoot = mainToolByRoot + (key to MainTool.Rama) }
            }
        val selectCc: (() -> Unit)? =
            routeMainKey?.let { key ->
                { mainToolByRoot = mainToolByRoot + (key to MainTool.Cc) }
            }
        menuBindingsState.value = AppMenuBindings(
            openProject = { openProjectAction() },
            showAbout = { aboutOpen = true },
            showSettings = { settingsOpen = true },
            selectRama = selectRama,
            selectCc = selectCc,
            setTheme = onThemeSelected,
        )
    }

    LaunchedEffect(backStackEntry?.destination?.route, pendingFolderPicker) {
        if (
            backStackEntry?.destination?.hasRoute(RouteSelection::class) == true &&
            pendingFolderPicker
        ) {
            pendingFolderPicker = false
            folderPicker.launch()
        }
    }

    if (aboutOpen) {
        Dialog(onDismissRequest = { aboutOpen = false }) {
            Card(
                modifier = Modifier.widthIn(max = 520.dp).wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.padding(AppSpacing.md)) {
                    AboutContent()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        AppTextButton(text = "Cerrar", onClick = { aboutOpen = false })
                    }
                }
            }
        }
    }

    if (settingsOpen) {
        Dialog(onDismissRequest = { settingsOpen = false }) {
            Card(
                modifier = Modifier.widthIn(max = 560.dp).wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
            ) {
                SettingsDialogContent(
                    initialPrefs = ccPrefs,
                    onDismiss = { settingsOpen = false },
                    onSave = { prefs ->
                        DesktopPreferencesStore.saveCcPrefs(prefs)
                        onCcPrefsChange(prefs)
                    },
                )
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = RouteSelection,
    ) {
        composable<RouteSelection> {
            ProjectSelectScreen(
                errorMessage = selectionError,
                onDismissError = { selectionError = null },
                recentProjects = recentProjects,
                onChooseFolder = {
                    selectionError = null
                    folderPicker.launch()
                },
                onRefreshRecent = {
                    recentProjects = ProjectHistoryStore.refresh()
                },
                onOpenRecent = { root ->
                    selectionError = null
                    if (!Files.isDirectory(root.resolve(".git"))) {
                        ProjectHistoryStore.remove(root)
                        recentProjects = ProjectHistoryStore.load()
                        selectionError =
                            "El proyecto ya no existe o no es un repo Git. Se quitó del historial."
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

        composable<RoutePendingToml> { entry ->
            val route = entry.toRoute<RoutePendingToml>()
            val root = Path.of(route.root)
            val goMainWithoutConfig = {
                ProjectHistoryStore.add(root)
                recentProjects = ProjectHistoryStore.load()
                navController.navigate(RouteMain(route.root)) {
                    popUpTo<RoutePendingToml> { inclusive = true }
                }
            }
            PendingTomlDialog(
                onDismissRequest = goMainWithoutConfig,
                onCreate = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val text = ExampleTomlTemplate.loadText()
                            Files.writeString(root.resolve(BnConfig.FILE_NAME), text)
                            BnConfig.load(root.resolve(BnConfig.FILE_NAME))
                            ProjectHistoryStore.add(root)
                            withContext(Dispatchers.Main) {
                                recentProjects = ProjectHistoryStore.load()
                                selectionError = null
                                navController.navigate(RouteMain(route.root)) {
                                    popUpTo<RoutePendingToml> { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            DesktopLog.error("PendingToml create failed", e)
                            withContext(Dispatchers.Main) {
                                selectionError = e.message ?: e.javaClass.simpleName
                                navigateToSelectionClearingStack()
                            }
                        }
                    }
                },
                onOmit = goMainWithoutConfig,
            )
        }

        composable<RouteMain> { entry ->
            val route = entry.toRoute<RouteMain>()
            val projectRoot = Path.of(route.root)
            var bnConfig by remember(route.root) { mutableStateOf<BnConfig?>(null) }
            var loadError by remember(route.root) { mutableStateOf<String?>(null) }

            LaunchedEffect(route.root) {
                loadError = null
                val cfgPath = projectRoot.resolve(BnConfig.FILE_NAME)
                bnConfig = if (!cfgPath.isRegularFile()) {
                    null
                } else {
                    try {
                        withContext(Dispatchers.IO) { BnConfig.load(cfgPath) }
                    } catch (e: Exception) {
                        DesktopLog.error("Failed to load ${BnConfig.FILE_NAME}", e)
                        loadError = e.message ?: e.javaClass.simpleName
                        null
                    }
                }
            }

            LaunchedEffect(loadError) {
                if (loadError != null) {
                    selectionError = "No se pudo leer ${BnConfig.FILE_NAME}: $loadError"
                    navigateToSelectionClearingStack()
                }
            }

            var tomlStatus by remember(projectRoot) { mutableStateOf<TomlUiStatus?>(null) }
            LaunchedEffect(projectRoot) {
                tomlStatus = withContext(Dispatchers.IO) { tomlUiStatus(projectRoot) }
            }

            val toolForRoute = mainToolByRoot[route.root]

            if (loadError == null) {
                MainShell(
                    projectRoot = projectRoot,
                    bnConfig = bnConfig,
                    tomlStatus = tomlStatus,
                    ccPrefs = ccPrefs,
                    mainTool = toolForRoute,
                    onMainToolChange = { t ->
                        mainToolByRoot = mainToolByRoot + (route.root to t)
                    },
                    themeMode = themeMode,
                    onThemeModeChange = onThemeSelected,
                    onOpenSettings = { settingsOpen = true },
                    onChangeProject = {
                        recentProjects = ProjectHistoryStore.load()
                        navigateToSelectionClearingStack()
                    },
                    onAboutClick = { aboutOpen = true },
                )
            }
        }
    }
}
