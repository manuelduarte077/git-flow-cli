package dev.donmanuel.cli.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BranchNameBuilderTest {

    @Test
    fun release() {
        assertEquals(
            "release/BNMP_V58-Sprint22.05",
            BranchNameBuilder.build(
                BranchNameBuilder.TipoRama.RELEASE,
                "BNMP",
                "V58-Sprint22.05",
                null,
                null,
                null,
            ),
        )
    }

    @Test
    fun feature() {
        assertEquals(
            "feature/BNMP_V58-Sprint22.05_DCSTI_BABEL_HU-116268",
            BranchNameBuilder.build(
                BranchNameBuilder.TipoRama.FEATURE,
                "BNMP",
                "V58-Sprint22.05",
                "DCSTI",
                "BABEL",
                "HU-116268",
            ),
        )
    }

    @Test
    fun releaseRejectsExtraFields() {
        assertEquals(
            "release/X_Y",
            BranchNameBuilder.build(
                BranchNameBuilder.TipoRama.RELEASE,
                "X",
                "Y",
                null,
                null,
                null,
            ),
        )
    }

    @Test
    fun featureRequiresArea() {
        assertFailsWith<IllegalArgumentException> {
            BranchNameBuilder.build(
                BranchNameBuilder.TipoRama.FEATURE,
                "A",
                "B",
                "",
                "E",
                "HU-1",
            )
        }
    }
}
