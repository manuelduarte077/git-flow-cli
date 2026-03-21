package dev.donmanuel.cli.core

import java.nio.charset.StandardCharsets

class GitService {

    fun createBranch(branchName: String) {
        runInheritIo("git", "checkout", "-b", branchName)
    }

    fun addAll() {
        runInheritIo("git", "add", ".")
    }

    fun commit(message: String) {
        val pb = ProcessBuilder("git", "commit", "-m", message)
        pb.redirectErrorStream(true)
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
            .start()
        return p.waitFor() == 0
    }

    fun hasStagedChanges(): Boolean {
        val p = ProcessBuilder("git", "diff", "--cached", "--quiet")
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .start()
        return p.waitFor() == 1
    }

    private fun runInheritIo(vararg command: String) {
        val process = ProcessBuilder(*command)
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            error("Error ejecutando: ${command.joinToString(" ")}")
        }
    }
}
