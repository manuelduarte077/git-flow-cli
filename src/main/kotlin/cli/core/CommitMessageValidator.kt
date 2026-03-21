package dev.donmanuel.cli.core

/**
 * Valida mensajes de commit con formato pipe:
 * `canal|subcanal|empresa|ticket| descripción`
 */
object CommitMessageValidator {

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
                    "Formato: canal|subcanal|empresa|ticket| descripción",
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

    sealed class ValidationResult {
        data object Ok : ValidationResult()
        data object Skipped : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
