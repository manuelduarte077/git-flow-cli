package dev.donmanuel.cli.config

import org.tomlj.Toml
import java.nio.file.Path

/**
 * Configuración por proyecto ([.git-bn-cli.toml] en la raíz del repo).
 */
data class BnConfig(
    val canal: String,
    val subcanal: String,
    val empresa: String,
    val siglasApp: String? = null,
) {
    companion object {
        const val FILE_NAME = ".git-bn-cli.toml"

        fun load(path: Path): BnConfig {
            val parse = Toml.parse(path)
            val canal = parse.getString("canal")
                ?: error("Falta clave 'canal' en $path")
            val subcanal = parse.getString("subcanal")
                ?: error("Falta clave 'subcanal' en $path")
            val empresa = parse.getString("empresa")
                ?: error("Falta clave 'empresa' en $path")
            val siglasApp = parse.getString("siglas_app")
            return BnConfig(
                canal = canal,
                subcanal = subcanal,
                empresa = empresa,
                siglasApp = siglasApp,
            )
        }
    }
}
