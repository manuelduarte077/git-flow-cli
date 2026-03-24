package dev.donmanuel.desktop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.desktop.theme.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTomlDialog(
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
    onOmit: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Configuración del proyecto", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "No existe ${BnConfig.FILE_NAME} en la raíz del repositorio. " +
                        "¿Crearlo a partir de la plantilla (equivalente a git-flow-cli.example.toml)?",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                AppTextButton(text = "Crear", onClick = onCreate)
            },
            dismissButton = {
                AppTextButton(text = "Omitir", onClick = onOmit)
            },
        )
    }
}
