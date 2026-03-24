package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.donmanuel.cli.CliVersion
import dev.donmanuel.cli.VersionCompare
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

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
            echo("No se pudo consultar GitHub (red o API).")
            return
        }
        echo("Última release en GitHub: $latest")
        when {
            VersionCompare.isRemoteNewer(latest, v) ->
                echo("Hay una versión más nueva. Descárgala en ${CliVersion.REPO_URL}/releases")

            VersionCompare.versionsEqual(latest, v) ->
                echo("Estás en la última release publicada.")

            else ->
                echo("Versión local $v vs release $latest (más nueva local o tag distinto).")
        }
    }

    private fun fetchLatestReleaseTag(): String? {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/manuelduarte077/git-flow-cli/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "git-flow-cli")
            .timeout(Duration.ofSeconds(8))
            .GET()
            .build()
        return try {
            val response = client.send(request, BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                return null
            }
            Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(response.body())?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }
}
