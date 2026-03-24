package dev.donmanuel.cli.core

object BranchNameBuilder {

    enum class TipoRama(val id: String) {
        FEATURE("feature"),
        HOTFIX("hotfix"),
        RELEASE("release"),
    }

    fun build(
        tipo: TipoRama,
        siglasApp: String,
        versionSprint: String,
        area: String?,
        empresa: String?,
        refHu: String?,
    ): String {
        val app = siglasApp.trim()
        val sprint = versionSprint.trim()
        require(app.isNotEmpty()) { "siglas de aplicación vacías" }
        require(sprint.isNotEmpty()) { "versión sprint vacía" }

        return when (tipo) {
            TipoRama.RELEASE -> {
                "release/${app}_${sprint}"
            }

            TipoRama.FEATURE, TipoRama.HOTFIX -> {
                val a = area?.trim().orEmpty()
                val e = empresa?.trim().orEmpty()
                val hu = refHu?.trim().orEmpty()
                require(a.isNotEmpty()) { "area requerida para ${tipo.id}" }
                require(e.isNotEmpty()) { "empresa requerida para ${tipo.id}" }
                require(hu.isNotEmpty()) { "referencia HU/ticket requerida para ${tipo.id}" }
                "${tipo.id}/${app}_${sprint}_${a}_${e}_$hu"
            }
        }
    }

    fun parseTipo(s: String): TipoRama {
        val t = s.trim().lowercase()
        return TipoRama.entries.find { it.id == t }
            ?: error("Tipo de rama inválido: $s. Use: feature, hotfix, release.")
    }
}
