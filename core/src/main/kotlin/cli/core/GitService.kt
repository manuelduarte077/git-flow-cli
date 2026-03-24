package dev.donmanuel.cli.core

import java.nio.charset.StandardCharsets
import java.nio.file.Path

class GitService(
    /** Si no es null, los comandos `git` se ejecutan con este directorio de trabajo. */
    private val repoRoot: Path? = null,
) {

    fun createBranch(branchName: String) {
        runInheritIo("git", "checkout", "-b", branchName)
    }

    fun addAll() {
        runInheritIo("git", "add", ".")
    }

    fun commit(message: String) {
        val pb = ProcessBuilder("git", "commit", "-m", message)
            .redirectErrorStream(true)
        applyWorkingDirectory(pb)
        val p = pb.start()
        val combined = p.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val exit = p.waitFor()
        if (exit != 0) {
            val detail = combined.trim().ifEmpty { "código de salida $exit" }
            error("git commit falló: $detail")
        }
    }

    fun isInsideGitWorkTree(): Boolean {
        val p = ProcessBuilder("git", "rev-parse", "--is-inside-work-tree")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
        applyWorkingDirectory(p)
        return p.start().waitFor() == 0
    }

    fun hasStagedChanges(): Boolean {
        val p = ProcessBuilder("git", "diff", "--cached", "--quiet")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
        applyWorkingDirectory(p)
        return p.start().waitFor() == 1
    }

    /** Rama actual (`git rev-parse --abbrev-ref HEAD`), o null si falla. */
    fun currentBranchName(): String? {
        val p = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
        applyWorkingDirectory(p)
        val proc = p.start()
        val out = proc.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }.trim()
        return if (proc.waitFor() == 0 && out.isNotEmpty()) out else null
    }

    private fun applyWorkingDirectory(pb: ProcessBuilder) {
        val root = repoRoot ?: return
        pb.directory(root.toFile())
    }

    private fun runInheritIo(vararg command: String) {
        val process = ProcessBuilder(*command)
            .inheritIO()
        applyWorkingDirectory(process)
        val exitCode = process.start().waitFor()
        if (exitCode != 0) {
            error("Error ejecutando: ${command.joinToString(" ")}")
        }
    }
}
