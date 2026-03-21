package dev.donmanuel.cli

object VersionCompare {

    fun versionsEqual(tag: String, local: String): Boolean =
        normalize(tag) == normalize(local)

    fun isRemoteNewer(remoteTag: String, local: String): Boolean {
        val r = normalize(remoteTag)
        val l = normalize(local)
        val n = maxOf(r.size, l.size)
        for (i in 0 until n) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    fun normalize(v: String): List<Int> =
        v.removePrefix("v").trim().split('.').map { it.toIntOrNull() ?: 0 }
}
