package dev.donmanuel.cli.core

object CommitMessageValidator {

    /** Texto único para mensajes de ayuda (stderr, UsageError, documentación). */
    const val FORMAT_EXPECTED_HINT = "canal|subcanal|empresa|ticket| descripción"

    fun validate(firstLine: String): ValidationResult {
        val line = firstLine.trim()
        if (line.isEmpty()) {
            return ValidationResult.Invalid("El mensaje de commit está vacío.")
        }
        if (line.startsWith("#") || line.startsWith("Merge ")) {
            return ValidationResult.Skipped
        }
        val parts = line.split('|')
        if (parts.size != 5) {
            return ValidationResult.Invalid(
                "Se esperan exactamente 5 segmentos separados por '|'. Encontrados: ${parts.size}. " +
                        "Formato: $FORMAT_EXPECTED_HINT",
            )
        }
        val trimmed = parts.map { it.trim() }
        for (i in 0..3) {
            if (trimmed[i].isEmpty()) {
                return ValidationResult.Invalid("El segmento ${i + 1} no puede estar vacío.")
            }
        }
        if (trimmed[4].isEmpty()) {
            return ValidationResult.Invalid("La descripción (tras el último '|') no puede estar vacía.")
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
