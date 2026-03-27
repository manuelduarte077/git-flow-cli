package dev.donmanuel.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.desktop.storage.DesktopPreferencesStore
import dev.donmanuel.desktop.theme.AppOutlinedTextField
import dev.donmanuel.desktop.theme.AppSpacing
import dev.donmanuel.desktop.theme.AppTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialogContent(
    initialPrefs: DesktopPreferencesStore.CcPrefs,
    onDismiss: () -> Unit,
    onSave: (DesktopPreferencesStore.CcPrefs) -> Unit,
) {
    var canal by remember(initialPrefs) { mutableStateOf(initialPrefs.canal.orEmpty()) }
    var subcanal by remember(initialPrefs) { mutableStateOf(initialPrefs.subcanal.orEmpty()) }
    var empresa by remember(initialPrefs) { mutableStateOf(initialPrefs.empresa.orEmpty()) }

    Column(
        Modifier.widthIn(min = 400.dp, max = 520.dp).padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Valores por defecto para el formulario Commit cuando no hay ${BnConfig.FILE_NAME} " +
                "(el TOML del repo sigue teniendo prioridad).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppOutlinedTextField(
            value = canal,
            onValueChange = { canal = it },
            label = { Text("Canal (vacío = predeterminado del proyecto)") },
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
            label = { Text("Empresa (vacío = predeterminado)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            AppTextButton(text = "Cancelar", onClick = onDismiss)
            AppTextButton(
                text = "Guardar",
                onClick = {
                    onSave(
                        DesktopPreferencesStore.CcPrefs(
                            canal = canal.takeIf { it.isNotBlank() },
                            subcanal = subcanal.takeIf { it.isNotBlank() },
                            empresa = empresa.takeIf { it.isNotBlank() },
                        ),
                    )
                    onDismiss()
                },
            )
        }
    }
}
