package dev.donmanuel.cli.config

/**
 * Valores de canal / subcanal / empresa para el formulario de commit en desktop.
 * Orden: TOML cargado del repo ([toml]) mayor que preferencias de app; si no hay TOML, preferencias mayor que [BnDefaults].
 */
data class CcDisplayFields(
    val canal: String,
    val subcanal: String,
    val empresa: String,
)

object CcDisplayDefaultsResolver {
    fun resolve(
        toml: BnConfig?,
        prefCanal: String?,
        prefSubcanal: String?,
        prefEmpresa: String?,
    ): CcDisplayFields {
        if (toml != null) {
            return CcDisplayFields(
                canal = toml.canal,
                subcanal = toml.subcanal,
                empresa = toml.empresa,
            )
        }
        return CcDisplayFields(
            canal = prefCanal?.takeIf { it.isNotBlank() } ?: BnDefaults.CANAL_COMMIT,
            subcanal = prefSubcanal?.takeIf { it.isNotBlank() } ?: "",
            empresa = prefEmpresa?.takeIf { it.isNotBlank() } ?: BnDefaults.EMPRESA,
        )
    }
}
