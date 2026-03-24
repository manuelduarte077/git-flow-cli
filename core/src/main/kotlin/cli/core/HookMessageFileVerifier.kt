package dev.donmanuel.cli.core

import dev.donmanuel.cli.CliMessages
import dev.donmanuel.cli.config.ConfigFinder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Lógica compartida para validar el archivo de mensaje de commit usado por el hook `commit-msg`
 * (y pruebas unitarias sin `exitProcess`).
 */
object HookMessageFileVerifier {

    sealed class Outcome {
        data object Ok : Outcome()
        data object Skipped : Outcome()
        data class Invalid(val reason: String) : Outcome()
        data class Usage(val message: String) : Outcome()
    }

    fun verify(path: Path): Outcome {
        val normalized = path.toAbsolutePath().normalize()
        when {
            !Files.exists(normalized) ->
                return Outcome.Usage("No existe: ${normalized.absolutePathString()}")

            Files.isDirectory(normalized) ->
                return Outcome.Usage("Es un directorio, no un archivo: ${normalized.absolutePathString()}")

            !Files.isRegularFile(normalized) ->
                return Outcome.Usage("No es un archivo: ${normalized.absolutePathString()}")
        }

        val gitRoot = ConfigFinder.findGitRoot(normalized)
            ?: return Outcome.Usage(CliMessages.NOT_IN_GIT_REPO)
        val gitDir = try {
            GitRepo.gitDirectory(gitRoot).toRealPath()
        } catch (e: IllegalStateException) {
            return Outcome.Usage(e.message ?: ".git no disponible.")
        }
        val realFile = normalized.toRealPath()
        if (!realFile.startsWith(gitDir)) {
            return Outcome.Usage(
                "El archivo debe estar bajo ${gitDir.absolutePathString()} (recibido: ${realFile.absolutePathString()}).",
            )
        }

        val text = Files.readString(normalized)
        return when (val v = CommitMessageValidator.validateMessageText(text)) {
            CommitMessageValidator.ValidationResult.Ok -> Outcome.Ok
            CommitMessageValidator.ValidationResult.Skipped -> Outcome.Skipped
            is CommitMessageValidator.ValidationResult.Invalid -> Outcome.Invalid(v.reason)
        }
    }

    fun verify(pathString: String): Outcome =
        verify(Path.of(pathString).toAbsolutePath().normalize())
}
