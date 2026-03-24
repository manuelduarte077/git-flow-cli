package dev.donmanuel.cli.core

import dev.donmanuel.cli.config.BnDefaults

/**
 * Valida nombres de rama alineados con [BranchNameBuilder] (prefijos feature/, hotfix/, release/).
 * Ramas genéricas (main, master, …) o sin prefijo BN no se rechazan ([Skipped]).
 */
object BranchNameValidator {

    const val FORMAT_HINT =
        "release/<siglas>_<sprint> · feature|hotfix/<siglas>_<sprint>_<area>_<empresa>_<refHU>"

    private val ALLOWLIST =
        setOf("main", "master", "develop", "dev", "staging", "head")

    fun validate(branchName: String): ValidationResult {
        val trimmed = branchName.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Nombre de rama vacío.")
        }
        if (trimmed.lowercase() in ALLOWLIST) {
            return ValidationResult.Skipped
        }
        val isBn =
            trimmed.startsWith("feature/") ||
                    trimmed.startsWith("hotfix/") ||
                    trimmed.startsWith("release/")
        if (!isBn) {
            return ValidationResult.Skipped
        }
        return validateBnFormat(trimmed)
    }

    private fun validateBnFormat(name: String): ValidationResult {
        when {
            name.startsWith("release/") -> {
                val rest = name.removePrefix("release/")
                if (rest.isEmpty()) {
                    return ValidationResult.Invalid("release/: falta <siglas>_<sprint>.")
                }
                val idx = rest.indexOf('_')
                if (idx <= 0) {
                    return ValidationResult.Invalid("release/: falta '_' entre siglas y sprint.")
                }
                val app = rest.substring(0, idx).trim()
                val sprint = rest.substring(idx + 1).trim()
                if (app.isEmpty() || sprint.isEmpty()) {
                    return ValidationResult.Invalid("release/: siglas o sprint vacíos.")
                }
                return ValidationResult.Ok
            }

            name.startsWith("feature/") || name.startsWith("hotfix/") -> {
                val prefix = if (name.startsWith("feature/")) "feature/" else "hotfix/"
                val rest = name.removePrefix(prefix)
                val parts = rest.split("_")
                if (parts.size < 5) {
                    return ValidationResult.Invalid(
                        "Tras ${prefix} hacen falta 5 segmentos con '_' (hay ${parts.size}). $FORMAT_HINT",
                    )
                }
                val app = parts[0].trim()
                val sprint = parts[1].trim()
                val area = parts[2].trim()
                val empresa = parts[3].trim()
                val hu = parts.drop(4).joinToString("_").trim()
                if (listOf(app, sprint, area, empresa, hu).any { it.isEmpty() }) {
                    return ValidationResult.Invalid("Segmento vacío en feature/hotfix.")
                }
                if (empresa != BnDefaults.EMPRESA) {
                    return ValidationResult.Invalid(
                        "Empresa debe ser ${BnDefaults.EMPRESA}; recibido: $empresa",
                    )
                }
                return ValidationResult.Ok
            }

            else -> return ValidationResult.Skipped
        }
    }

    sealed class ValidationResult {
        data object Ok : ValidationResult()
        data object Skipped : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
}
