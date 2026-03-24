package dev.donmanuel.desktop.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.desktop.generated.Res
import dev.donmanuel.desktop.generated.ic_bn
import dev.donmanuel.desktop.theme.AppSpacing
import dev.donmanuel.desktop.theme.AppTextButton
import org.jetbrains.compose.resources.painterResource
import java.nio.file.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    projectRoot: Path,
    bnConfig: BnConfig?,
    tomlStatus: TomlUiStatus?,
    onChangeProject: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var currentTool by remember { mutableStateOf<MainTool?>(null) }
    val titlePath = remember(projectRoot) {
        projectRoot.toAbsolutePath().toString().let { s ->
            if (s.length > 52) "…" + s.takeLast(48) else s
        }
    }
    val railItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            header = {
                Image(
                    painter = painterResource(Res.drawable.ic_bn),
                    contentDescription = "Git BN Flow",
                    modifier = Modifier
                        .padding(vertical = AppSpacing.sm)
                        .size(40.dp),
                )
            },
        ) {
            Spacer(Modifier.height(AppSpacing.xs))
            NavigationRailItem(
                icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Cambiar proyecto") },
                label = { Text("Proyecto", style = MaterialTheme.typography.labelLarge) },
                selected = false,
                onClick = onChangeProject,
                colors = railItemColors,
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.AccountTree, contentDescription = "Rama") },
                label = { Text("Rama", style = MaterialTheme.typography.labelLarge) },
                selected = currentTool == MainTool.Rama,
                onClick = { currentTool = MainTool.Rama },
                colors = railItemColors,
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.EditNote, contentDescription = "Commit") },
                label = { Text("Commit", style = MaterialTheme.typography.labelLarge) },
                selected = currentTool == MainTool.Cc,
                onClick = { currentTool = MainTool.Cc },
                colors = railItemColors,
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(AppSpacing.sm))
        }

        Scaffold(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "GIT BN FLOW",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                titlePath,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    actions = {
                        if (tomlStatus == TomlUiStatus.Missing) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Sin TOML") },
                                colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.error),
                            )
                        }
                        AppTextButton(text = "Cambiar proyecto", onClick = onChangeProject)
                        IconButton(onClick = onAboutClick) {
                            Icon(Icons.Default.Info, contentDescription = "Acerca de")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
        ) { padding ->
            Box(
                Modifier.fillMaxSize().padding(padding).padding(AppSpacing.md),
            ) {
                when (currentTool) {
                    null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Text(
                                "Elige Rama o Commit en la barra lateral.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    MainTool.Rama -> {
                        RamaPanel(
                            projectRoot = projectRoot,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    MainTool.Cc -> {
                        CcPanel(
                            projectRoot = projectRoot,
                            bnConfig = bnConfig,
                            tomlStatus = tomlStatus,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
