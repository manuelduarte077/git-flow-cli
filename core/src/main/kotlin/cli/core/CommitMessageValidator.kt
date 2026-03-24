package dev.donmanuel.cli.core

object CommitMessageValidator {

    /** Texto único para mensajes de ayuda (stderr, UsageError, documentación). */
    const val FORMAT_EXPECTED_HINT = "canal|subcanal|empresa|ticket| descripción"

    fun validate(firstLine: String): ValidationResult {
        val line = firstLine.trim()
        if (line.isEmpty()) {
            return ValidationResult.Invalid("Mensaje de commit vacío.")
        }
        if (line.startsWith("#") || line.startsWith("Merge ")) {
            return ValidationResult.Skipped
        }
        val parts = line.split('|')
        if (parts.size != 5) {
            return ValidationResult.Invalid(
                "Hace falta 5 segmentos con '|' (hay ${parts.size}). Formato: $FORMAT_EXPECTED_HINT",
            )
        }
        val trimmed = parts.map { it.trim() }
        for (i in 0..3) {
            if (trimmed[i].isEmpty()) {
                return ValidationResult.Invalid("Segmento ${i + 1} vacío.")
            }
        }
        if (trimmed[4].isEmpty()) {
            return ValidationResult.Invalid("Descripción vacía.")
        }
        return ValidationResult.Ok
    }

    /**
     * Primera línea no vacía ni comentario `#` (como en un archivo de mensaje de commit).
     */
    fun firstMeaningfulLine(text: String): String =
        text.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() && !it.startsWith("#") }
            ?: ""

    fun validateMessageText(fullMessage: String): ValidationResult =
        validate(firstMeaningfulLine(fullMessage))

    sealed class ValidationResult {
        data object Ok : ValidationResult()
        data object Skipped : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
