package dev.donmanuel.cli.config

import dev.donmanuel.cli.config.BnDefaults
import kotlin.test.Test
import kotlin.test.assertEquals

class CcDisplayDefaultsResolverTest {

    @Test
    fun tomlWinsOverPrefs() {
        val toml = BnConfig(canal = "c1", subcanal = "s1", empresa = "e1")
        val r = CcDisplayDefaultsResolver.resolve(
            toml,
            prefCanal = "pc",
            prefSubcanal = "ps",
            prefEmpresa = "pe",
        )
        assertEquals("c1", r.canal)
        assertEquals("s1", r.subcanal)
        assertEquals("e1", r.empresa)
    }

    @Test
    fun prefsFillWhenNoToml() {
        val r = CcDisplayDefaultsResolver.resolve(
            toml = null,
            prefCanal = "my_canal",
            prefSubcanal = "my_sub",
            prefEmpresa = "my_emp",
        )
        assertEquals("my_canal", r.canal)
        assertEquals("my_sub", r.subcanal)
        assertEquals("my_emp", r.empresa)
    }

    @Test
    fun defaultsWhenNoTomlAndEmptyPrefs() {
        val r = CcDisplayDefaultsResolver.resolve(
            toml = null,
            prefCanal = null,
            prefSubcanal = null,
            prefEmpresa = null,
        )
        assertEquals(BnDefaults.CANAL_COMMIT, r.canal)
        assertEquals("", r.subcanal)
        assertEquals(BnDefaults.EMPRESA, r.empresa)
    }
}
