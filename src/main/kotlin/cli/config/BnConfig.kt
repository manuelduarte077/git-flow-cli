package dev.donmanuel.cli.config

import org.tomlj.Toml
import java.nio.file.Path

data class BnConfig(
    val canal: String,
    val subcanal: String,
    val empresa: String,
    val siglasApp: String? = null,
) {
    companion object {
        const val FILE_NAME = ".git-flow-cli.toml"

        fun load(path: Path): BnConfig {
            val parse = Toml.parse(path)
            val canal = parse.getString("canal") ?: BnDefaults.CANAL_COMMIT
            val subcanal = parse.getString("subcanal")
                ?: error("Falta clave 'subcanal' en $path")
            val empresa = parse.getString("empresa") ?: BnDefaults.EMPRESA
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
