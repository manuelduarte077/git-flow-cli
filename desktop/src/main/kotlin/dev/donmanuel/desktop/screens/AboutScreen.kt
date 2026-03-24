package dev.donmanuel.desktop.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.donmanuel.cli.CliVersion
import dev.donmanuel.desktop.theme.AppSpacing
import java.awt.Desktop
import java.net.URI

private val releasesUrl: String
    get() = "${CliVersion.REPO_URL}/releases"

private fun openUrlInBrowser(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url))
        }
    }
}

@Composable
private fun ClickableLink(text: String, url: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        ),
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { openUrlInBrowser(url) },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun AboutContent(modifier: Modifier = Modifier) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = "Git BN Flow · Escritorio",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Versión ${CliVersion.current()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider(Modifier.padding(vertical = AppSpacing.xs))

        SectionTitle("Qué hace")
        Text(
            text = "Te ayuda a generar nombres de rama con el formato BN " +
                "(feature, hotfix, release) y líneas de commit tipo " +
                "canal|subcanal|empresa|ticket| descripción, usando la misma lógica que la CLI git-flow-cli.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SectionTitle("Por qué se desarrolló")
        Text(
            text = "Para que equipos que ya siguen convenciones Git Flow no dependan solo del terminal: " +
                "menos errores al copiar y pegar, y un flujo guiado. La app de escritorio es un complemento; " +
                "los hooks de Git pueden seguir invocando el binario instalado en PATH.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        HorizontalDivider(Modifier.padding(vertical = AppSpacing.xs))

        SectionTitle("Enlaces")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ClickableLink(text = "Repositorio en GitHub", url = CliVersion.REPO_URL)
            ClickableLink(text = "Descargas y releases", url = releasesUrl)
        }

        Text(
            text = "Los hooks de Git siguen usando el ejecutable git-flow-cli del PATH.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.sm),
        )
    }
}

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    AboutContent(modifier)
}
