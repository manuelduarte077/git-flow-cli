package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.CliVersion
import dev.donmanuel.cli.VersionCompare
import java.net.HttpURLConnection
import java.net.URL

class AboutCommand : CliktCommand(
    name = "about",
    help = "Muestra versión instalada, enlace al proyecto y opcionalmente si hay una release más nueva.",
) {

    private val checkUpdates by option(
        "--check-updates",
        help = "Consulta GitHub y compara con la versión instalada (requiere red).",
    ).flag(default = false)

    override fun run() {
        val v = CliVersion.current()
        echo("git-flow-cli $v")
        echo("Repositorio: ${CliVersion.REPO_URL}")
        echo("Releases: ${CliVersion.REPO_URL}/releases")

        if (!checkUpdates) {
            echo("")
            echo("Para comprobar actualizaciones: git-flow-cli about --check-updates")
            return
        }

        echo("")
        val latest = fetchLatestReleaseTag()
        if (latest == null) {
            echo("No se pudo consultar la última versión en GitHub (sin red o API no disponible).")
            return
        }
        echo("Última release en GitHub: $latest")
        when {
            VersionCompare.isRemoteNewer(latest, v) ->
                echo("Hay una versión más nueva. Descárgala en ${CliVersion.REPO_URL}/releases")

            VersionCompare.versionsEqual(latest, v) ->
                echo("Estás en la última release publicada.")

            else ->
                echo("Tu versión ($v) es más reciente que la última release listada ($latest), o el formato no coincide.")
        }
    }

    private fun fetchLatestReleaseTag(): String? {
        val url = URL("https://api.github.com/repos/manuelduarte077/git-flow-cli/releases/latest")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "git-flow-cli")
        return try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(text)?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

}
