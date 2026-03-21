package dev.donmanuel.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionCompareTest {

    @Test
    fun normalize_stripsV_and_splits() {
        assertEquals(listOf(2, 0, 0), VersionCompare.normalize("v2.0.0"))
        assertEquals(listOf(2, 0, 0), VersionCompare.normalize("2.0.0"))
    }

    @Test
    fun versionsEqual_ignoresVPrefix() {
        assertTrue(VersionCompare.versionsEqual("v2.0.0", "2.0.0"))
    }

    @Test
    fun isRemoteNewer_comparesSemverParts() {
        assertTrue(VersionCompare.isRemoteNewer("v2.1.0", "2.0.0"))
        assertFalse(VersionCompare.isRemoteNewer("v2.0.0", "2.1.0"))
        assertFalse(VersionCompare.isRemoteNewer("2.0.0", "2.0.0"))
    }
}
