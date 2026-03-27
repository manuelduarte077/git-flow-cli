package dev.donmanuel.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import dev.donmanuel.cli.CliVersion
import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ConfigFinder
import dev.donmanuel.cli.core.GitRepo
import dev.donmanuel.cli.core.GitService
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class DoctorCommand : CliktCommand(
    name = "doctor",
    help = "Comprueba Java, git en PATH, repo git, TOML, hooks instalados y versión del CLI.",
) {
    override fun run() {
        echo("git-flow-cli doctor")
        echo("")

        val ok = mutableListOf<String>()
        val warn = mutableListOf<String>()
        val fail = mutableListOf<String>()

        ok.add("Versión CLI: ${CliVersion.current()}")
        ok.add("Java runtime: ${Runtime.version()}")

        val gitLine = try {
            val p = ProcessBuilder("git", "--version").redirectErrorStream(true).start()
            val out = p.inputStream.bufferedReader().use { it.readText().trim() }
            if (p.waitFor() == 0) out else null
        } catch (_: Exception) {
            null
        }
        if (gitLine != null) {
            ok.add("git: $gitLine")
        } else {
            fail.add("git: no se pudo ejecutar (¿instalado y en PATH?)")
        }

        val gs = GitService()
        if (!gs.isInsideGitWorkTree()) {
            warn.add("No estás dentro de un work tree de Git (rama/TOML/hooks no comprobados aquí).")
            printSections(ok, warn, fail)
            return
        }

        ok.add("Git work tree: sí")

        val root = ConfigFinder.findGitRoot()
        if (root == null) {
            warn.add("No se encontró la raíz del repositorio (.git).")
            printSections(ok, warn, fail)
            return
        }

        ok.add("Raíz del repo (absoluta): ${root.toAbsolutePath().normalize()}")

        val cfgPath = ConfigFinder.findConfigFile()
        if (cfgPath != null) {
            ok.add("TOML: ${cfgPath.toAbsolutePath().normalize()} (${BnConfig.FILE_NAME} o legado)")
        } else {
            warn.add("TOML: no hay ${BnConfig.FILE_NAME} (opcional; `git-flow-cli init` crea uno).")
        }

        val hooksDir = GitRepo.resolveHooksDirectory(root)
        ok.add("Directorio de hooks Git (absoluto): ${hooksDir.toAbsolutePath().normalize()}")

        val commitMsg = hooksDir.resolve("commit-msg")
        if (commitMsg.isRegularFile()) {
            val text = commitMsg.readText()
            if (text.contains(GitRepo.HOOK_MANAGED_MARKER)) {
                ok.add("Hook commit-msg: instalado por git-flow-cli (${commitMsg.toAbsolutePath().normalize()})")
            } else {
                warn.add(
                    "commit-msg existe pero no está gestionado por git-flow-cli " +
                        "(${commitMsg.toAbsolutePath().normalize()}).",
                )
            }
        } else {
            warn.add(
                "commit-msg: no instalado. Ruta esperada: ${commitMsg.toAbsolutePath().normalize()} " +
                    "(ej. `git-flow-cli hooks install`).",
            )
        }

        val postCheckout = hooksDir.resolve("post-checkout")
        if (postCheckout.isRegularFile() && postCheckout.readText().contains(GitRepo.HOOK_POST_CHECKOUT_MARKER)) {
            ok.add("Hook post-checkout (rama BN): instalado (${postCheckout.toAbsolutePath().normalize()})")
        } else {
            warn.add(
                "post-checkout (rama BN): no instalado. Ruta esperada: ${postCheckout.toAbsolutePath().normalize()} " +
                    "(`git-flow-cli hooks install --branch-hook`).",
            )
        }

        val desktopBundle = System.getenv("GIT_BN_FLOW_DESKTOP_BUNDLE")
        if (desktopBundle.isNullOrBlank()) {
            ok.add("Desktop empaquetado: N/A (opcional; defina GIT_BN_FLOW_DESKTOP_BUNDLE si aplica).")
        } else {
            ok.add("Desktop empaquetado: $desktopBundle")
        }

        printSections(ok, warn, fail)
    }

    private fun printSections(ok: List<String>, warn: List<String>, fail: List<String>) {
        if (ok.isNotEmpty()) {
            echo("OK")
            ok.forEach { echo("  • $it") }
            echo("")
        }
        if (warn.isNotEmpty()) {
            echo("Avisos")
            warn.forEach { echo("  • $it") }
            echo("")
        }
        if (fail.isNotEmpty()) {
            echo("Problemas")
            fail.forEach { echo("  • $it") }
        }
    }
}
