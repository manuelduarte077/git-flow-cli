package dev.donmanuel.cli.core

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HookMessageFileVerifierTest {

    @TempDir
    lateinit var repo: Path

    @Test
    fun `accepts valid pipe message under git dir`() {
        assumeGitAvailable()
        runGit("init")
        val msg = repo.resolve(".git/COMMIT_EDITMSG")
        Files.createDirectories(msg.parent)
        val line = "canales_digitales|canales_2|NOVACOMP|HU-116268| descripción de prueba"
        Files.writeString(msg, line)

        val o = HookMessageFileVerifier.verify(msg)
        assertIs<HookMessageFileVerifier.Outcome.Ok>(o)
    }

    @Test
    fun `rejects file outside git metadata tree`() {
        assumeGitAvailable()
        runGit("init")
        val outsideWorkTree = repo.resolve("commit-msg-outside-git.txt")
        Files.writeString(outsideWorkTree, "canales_digitales|canales_2|NOVACOMP|HU-1| ok")

        val o = HookMessageFileVerifier.verify(outsideWorkTree)
        assertIs<HookMessageFileVerifier.Outcome.Usage>(o)
        assertTrue(o.message.contains("debe estar bajo"))
    }

    private fun runGit(vararg args: String) {
        val p = ProcessBuilder(listOf("git", *args))
            .directory(repo.toFile())
            .redirectErrorStream(true)
            .start()
        p.inputStream.bufferedReader().use { it.readText() }
        val code = p.waitFor()
        kotlin.test.assertEquals(0, code, "git ${args.joinToString(" ")} falló")
    }

    private fun assumeGitAvailable() {
        val p = ProcessBuilder("git", "--version").redirectErrorStream(true).start()
        p.inputStream.bufferedReader().use { it.readText() }
        assumeTrue(p.waitFor() == 0) { "git no está en PATH" }
    }
}
