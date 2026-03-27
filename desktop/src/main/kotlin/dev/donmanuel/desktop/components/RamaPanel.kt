package dev.donmanuel.desktop.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.donmanuel.cli.config.BnDefaults
import dev.donmanuel.cli.core.BranchNameBuilder
import dev.donmanuel.cli.core.GitService
import dev.donmanuel.desktop.logging.DesktopLog
import dev.donmanuel.desktop.theme.AppOutlinedTextField
import dev.donmanuel.desktop.theme.AppPrimaryButton
import dev.donmanuel.desktop.theme.AppSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.absolutePathString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RamaPanel(
    projectRoot: Path,
    modifier: Modifier = Modifier,
) {
    val gs = remember(projectRoot) { GitService(repoRoot = projectRoot) }
    val scope = rememberCoroutineScope()
    var tipo by remember { mutableStateOf("feature") }
    var app by remember { mutableStateOf("") }
    var sprint by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var hu by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text("Nueva rama", style = MaterialTheme.typography.titleLarge)
        ProjectPathWithContextMenu(pathText = projectRoot.absolutePathString())
        AppOutlinedTextField(
            value = tipo,
            onValueChange = { tipo = it },
            label = { Text("Tipo (feature / hotfix / release)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = app,
            onValueChange = { app = it },
            label = { Text("Siglas aplicación") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = sprint,
            onValueChange = { sprint = it },
            label = { Text("Versión sprint") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("Área funcional (módulo o dominio)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = hu,
            onValueChange = { hu = it },
            label = { Text("HU / ticket") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppPrimaryButton(
            text = "Crear rama",
            onClick = {
                error = null
                status = ""
                scope.launch {
                    try {
                        val t = withContext(Dispatchers.Default) {
                            BranchNameBuilder.parseTipo(tipo)
                        }
                        val branch = BranchNameBuilder.build(
                            tipo = t,
                            siglasApp = app,
                            versionSprint = sprint,
                            area = area.takeIf { it.isNotBlank() },
                            empresa = when (t) {
                                BranchNameBuilder.TipoRama.RELEASE -> null
                                BranchNameBuilder.TipoRama.FEATURE,
                                BranchNameBuilder.TipoRama.HOTFIX,
                                -> BnDefaults.EMPRESA
                            },
                            refHu = hu.takeIf { it.isNotBlank() },
                        )
                        status = "Rama generada: $branch"
                        val ioErr = withContext(Dispatchers.IO) {
                            if (!gs.isInsideGitWorkTree()) {
                                return@withContext "No hay repositorio git en ${projectRoot.absolutePathString()}."
                            }
                            try {
                                gs.createBranch(branch)
                                null
                            } catch (e: IllegalStateException) {
                                e.message ?: "git checkout -b falló"
                            }
                        }
                        if (ioErr != null) {
                            error = ioErr
                        } else {
                            status = "Rama creada y checkout: $branch"
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        DesktopLog.error("Rama create failed", e)
                        error = e.message ?: e.javaClass.simpleName
                    }
                }
            },
            leadingIcon = Icons.Default.AccountTree,
        )
        error?.let { Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        if (status.isNotEmpty()) {
            Text(status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
