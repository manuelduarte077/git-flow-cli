package dev.donmanuel.desktop.components

import dev.donmanuel.cli.config.BnConfig
import dev.donmanuel.cli.config.ExampleTomlTemplate
import java.nio.file.Path
import kotlin.io.path.isRegularFile

enum class TomlUiStatus {
    Missing,
    SameAsExample,
    Custom,
}

fun tomlUiStatus(root: Path): TomlUiStatus {
    val p = root.resolve(BnConfig.FILE_NAME)
    if (!p.isRegularFile()) return TomlUiStatus.Missing
    return if (ExampleTomlTemplate.contentMatchesTemplate(p)) {
        TomlUiStatus.SameAsExample
    } else {
        TomlUiStatus.Custom
    }
}
