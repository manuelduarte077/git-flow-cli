package dev.donmanuel.cli.core

class GitService {

    fun createBranch(branchName: String) {
        run("git", "checkout", "-b", branchName)
    }

    fun addAll() {
        run("git", "add", ".")
    }

    fun commit(message: String) {
        run("git", "commit", "-m", message)
    }

    private fun run(vararg command: String) {
        val process = ProcessBuilder(*command)
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            error("Error ejecutando: ${command.joinToString(" ")}")
        }
    }
}