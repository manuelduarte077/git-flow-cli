package dev.donmanuel.cli.core

object Slugifier {
    fun toSlug(input: String): String {
        return input
            .lowercase()
            .replace("-", "_")
            .replace(Regex("[^a-z0-9-]"), "")
    }
}