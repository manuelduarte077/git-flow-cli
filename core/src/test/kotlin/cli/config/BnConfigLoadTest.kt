package dev.donmanuel.cli.config

import dev.donmanuel.cli.config.BnConfig
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import java.nio.file.Files

class BnConfigLoadTest {

    @Test
    fun missingSubcanalThrows() {
        val f = Files.createTempFile("bn-", ".toml")
        try {
            f.writeText("canal = \"x\"\n")
            assertFailsWith<IllegalStateException> {
                BnConfig.load(f)
            }
        } finally {
            runCatching { f.deleteExisting() }
        }
    }

    @Test
    fun invalidTomlThrows() {
        val f = Files.createTempFile("bn-", ".toml")
        try {
            f.writeText("[[[\n")
            assertFailsWith<IllegalStateException> {
                BnConfig.load(f)
            }
        } finally {
            runCatching { f.deleteExisting() }
        }
    }

    @Test
    fun minimalValidLoads() {
        val f = Files.createTempFile("bn-", ".toml")
        try {
            f.writeText("subcanal = \"sc1\"\n")
            val c = BnConfig.load(f)
            assertEquals("sc1", c.subcanal)
        } finally {
            runCatching { f.deleteExisting() }
        }
    }
}
