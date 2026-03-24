package dev.donmanuel.desktop.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.desktop.theme.AppPrimaryButton
import dev.donmanuel.desktop.theme.AppSpacing
import dev.donmanuel.desktop.theme.AppTextButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelectScreen(
    errorMessage: String?,
    onDismissError: () -> Unit,
    recentProjects: List<Path>,
    onChooseFolder: () -> Unit,
    onOpenRecent: (Path) -> Unit,
    onRemoveFromHistory: (Path) -> Unit,
) {
    var tomlStatusByRoot by remember { mutableStateOf<Map<Path, TomlUiStatus>>(emptyMap()) }
    LaunchedEffect(recentProjects) {
        val roots = recentProjects
        tomlStatusByRoot = withContext(Dispatchers.IO) {
            roots.associateWith { tomlUiStatus(it) }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text("GIT BN FLOW", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Selecciona un proyecto Git. Si ya tienes ${BnConfig.FILE_NAME}, se validará y cargará.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppPrimaryButton(
            text = "Elegir carpeta del repositorio",
            onClick = onChooseFolder,
            leadingIcon = Icons.Default.FolderOpen,
        )
        Text("Proyectos recientes", style = MaterialTheme.typography.titleMedium)
        if (recentProjects.isEmpty()) {
            Text(
                "Aún no hay historial. Abre un repo y quedará guardado aquí.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(recentProjects, key = { it.toString() }) { root ->
                    val status = tomlStatusByRoot[root]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenRecent(root) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Row(
                            Modifier.padding(AppSpacing.md).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    root.toAbsolutePath().toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                when (status) {
                                    null -> {}
                                    TomlUiStatus.Missing -> {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Sin ${BnConfig.FILE_NAME}", style = MaterialTheme.typography.labelLarge) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = MaterialTheme.colorScheme.error,
                                            ),
                                        )
                                    }
                                    TomlUiStatus.SameAsExample -> {}
                                    TomlUiStatus.Custom -> {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("TOML personalizado", style = MaterialTheme.typography.labelLarge) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = MaterialTheme.colorScheme.tertiary,
                                            ),
                                        )
                                    }
                                }
                            }
                            AppTextButton(text = "Quitar", onClick = { onRemoveFromHistory(root) })
                        }
                    }
                }
            }
        }
        errorMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(Modifier.padding(AppSpacing.md)) {
                    Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    AppTextButton(text = "Cerrar", onClick = onDismissError)
                }
            }
        }
    }
}
