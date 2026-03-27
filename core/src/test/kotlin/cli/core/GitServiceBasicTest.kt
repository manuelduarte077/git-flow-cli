package dev.donmanuel.cli.core
import kotlin.io.path.deleteRecursively
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.nio.file.Files

class GitServiceBasicTest {

    @Test
    fun notInsideGitWorkTreeInEmptyTempDir() {
        val dir = Files.createTempDirectory("git-flow-cli-gs-")
        try {
            val gs = GitService(repoRoot = dir)
            assertFalse(gs.isInsideGitWorkTree())
            assertNull(gs.currentBranchName())
            assertFalse(gs.hasStagedChanges())
        } finally {
            dir.toFile().deleteRecursively()
        }
    }
}
