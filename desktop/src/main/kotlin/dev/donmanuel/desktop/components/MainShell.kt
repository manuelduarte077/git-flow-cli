package dev.donmanuel.desktop.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.desktop.screens.AboutContent
import dev.donmanuel.desktop.theme.AppSpacing
import dev.donmanuel.desktop.theme.AppTextButton
import java.nio.file.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    projectRoot: Path,
    bnConfig: BnConfig?,
    tomlStatus: TomlUiStatus,
    onChangeProject: () -> Unit,
) {
    var currentTool by remember { mutableStateOf<MainTool?>(null) }
    var aboutOpen by remember { mutableStateOf(false) }
    val titlePath = remember(projectRoot) {
        projectRoot.toAbsolutePath().toString().let { s ->
            if (s.length > 52) "…" + s.takeLast(48) else s
        }
    }

    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Spacer(Modifier.height(AppSpacing.sm))
            NavigationRailItem(
                icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                label = { Text("Proyecto", style = MaterialTheme.typography.labelLarge) },
                selected = false,
                onClick = onChangeProject,
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                label = { Text("Rama", style = MaterialTheme.typography.labelLarge) },
                selected = currentTool == MainTool.Rama,
                onClick = { currentTool = MainTool.Rama },
            )
            NavigationRailItem(
                icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                label = { Text("Commit", style = MaterialTheme.typography.labelLarge) },
                selected = currentTool == MainTool.Cc,
                onClick = { currentTool = MainTool.Cc },
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("GIT BN FLOW", style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                titlePath,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        IconButton(onClick = { aboutOpen = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Acerca de")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
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
}
