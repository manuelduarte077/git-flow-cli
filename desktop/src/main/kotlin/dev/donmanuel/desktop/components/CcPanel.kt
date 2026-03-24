package dev.donmanuel.desktop.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.BnDefaults
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.CommitMessageValidator
import dev.donmanuel.cli.core.GitService
import dev.donmanuel.desktop.theme.AppOutlinedTextField
import dev.donmanuel.desktop.theme.AppPrimaryButton
import dev.donmanuel.desktop.theme.AppSecondaryButton
import dev.donmanuel.desktop.theme.AppSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.absolutePathString

@Composable
fun CcPanel(
    projectRoot: Path,
    bnConfig: BnConfig?,
    tomlStatus: TomlUiStatus,
    modifier: Modifier = Modifier,
) {
    val gs = remember(projectRoot) { GitService(repoRoot = projectRoot) }
    val scope = rememberCoroutineScope()
    var ticket by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var subcanal by remember { mutableStateOf("") }
    var canal by remember { mutableStateOf(BnDefaults.CANAL_COMMIT) }
    var empresa by remember { mutableStateOf(BnDefaults.EMPRESA) }
    var preview by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectRoot, bnConfig) {
        if (bnConfig != null) {
            canal = bnConfig.canal
            empresa = bnConfig.empresa
            subcanal = bnConfig.subcanal
        } else {
            val found = ConfigFinder.findConfigFile(projectRoot)
            if (found != null) {
                try {
                    val cfg = BnConfig.load(found)
                    canal = cfg.canal
                    empresa = cfg.empresa
                    subcanal = cfg.subcanal
                } catch (_: Exception) {
                    canal = BnDefaults.CANAL_COMMIT
                    empresa = BnDefaults.EMPRESA
                    subcanal = ""
                }
            } else {
                canal = BnDefaults.CANAL_COMMIT
                empresa = BnDefaults.EMPRESA
                subcanal = ""
            }
        }
    }

    Column(
        modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text("Commit (cc)", style = MaterialTheme.typography.titleLarge)
        Text(
            projectRoot.absolutePathString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (tomlStatus == TomlUiStatus.Missing) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "No hay ${BnConfig.FILE_NAME}. Valores por defecto; indica el subcanal.",
                    Modifier.padding(AppSpacing.md),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        AppOutlinedTextField(
            value = canal,
            onValueChange = { canal = it },
            label = { Text("Canal") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = subcanal,
            onValueChange = { subcanal = it },
            label = { Text("Subcanal") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = empresa,
            onValueChange = { empresa = it },
            label = { Text("Empresa") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = ticket,
            onValueChange = { ticket = it },
            label = { Text("Ticket") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AppOutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppSecondaryButton(
                text = "Vista previa",
                onClick = {
                    error = null
                    status = ""
                    val line = "$canal|$subcanal|$empresa|$ticket| ${descripcion.trim()}"
                    preview = line
                    when (val v = CommitMessageValidator.validate(line)) {
                        is CommitMessageValidator.ValidationResult.Invalid ->
                            error = v.reason

                        CommitMessageValidator.ValidationResult.Skipped,
                        CommitMessageValidator.ValidationResult.Ok,
                            -> Unit
                    }
                },
            )
            AppPrimaryButton(
                text = "Commit",
                onClick = {
                    error = null
                    status = ""
                    scope.launch {
                        try {
                            val line = "$canal|$subcanal|$empresa|$ticket| ${descripcion.trim()}"
                            when (val v = CommitMessageValidator.validate(line)) {
                                is CommitMessageValidator.ValidationResult.Invalid -> {
                                    error = v.reason
                                    return@launch
                                }

                                CommitMessageValidator.ValidationResult.Skipped,
                                CommitMessageValidator.ValidationResult.Ok,
                                    -> Unit
                            }
                            withContext(Dispatchers.IO) {
                                if (!gs.isInsideGitWorkTree()) {
                                    error = "No hay repositorio git en ${projectRoot.absolutePathString()}."
                                    return@withContext
                                }
                                if (!gs.hasStagedChanges()) {
                                    error = "Nada en staging. Usa git add antes de commit."
                                    return@withContext
                                }
                                gs.commit(line)
                            }
                            if (error == null) {
                                status = "Commit creado."
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = e.message ?: e.javaClass.simpleName
                        }
                    }
                },
                leadingIcon = Icons.Default.EditNote,
            )
        }
        error?.let {
            Text(
                "Error: $it",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (preview.isNotEmpty()) {
            Text("Mensaje", style = MaterialTheme.typography.titleSmall)
            Text(preview, style = MaterialTheme.typography.bodyMedium)
        }
        if (status.isNotEmpty()) {
            Text(status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
