package dev.donmanuel.cli.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CommitMessageValidatorTest {

    @Test
    fun validPipeMessage() {
        val r = CommitMessageValidator.validate(
            "canales_digitales|canales_2|NOVACOMP|BUG 886814| Implementación de validación",
        )
        assertIs<CommitMessageValidator.ValidationResult.Ok>(r)
    }

    @Test
    fun validWithHuFormat() {
        val r = CommitMessageValidator.validate(
            "canales_digitales|canales_2|NOVACOMP|MEJ-857175| No mostrar caracteres",
        )
        assertIs<CommitMessageValidator.ValidationResult.Ok>(r)
    }

    @Test
    fun wrongSegmentCount() {
        val r = CommitMessageValidator.validate("a|b|c|d")
        assertIs<CommitMessageValidator.ValidationResult.Invalid>(r)
    }

    @Test
    fun emptyDescription() {
        val r = CommitMessageValidator.validate("a|b|c|BUG 1| ")
        assertIs<CommitMessageValidator.ValidationResult.Invalid>(r)
    }

    @Test
    fun firstMeaningfulLineSkipsCommentsAndBlanks() {
        val text = """
            
            # comentario
            canales_digitales|canales_2|NOVACOMP|X| desc
        """.trimIndent()
        assertEquals(
            "canales_digitales|canales_2|NOVACOMP|X| desc",
            CommitMessageValidator.firstMeaningfulLine(text),
        )
    }

    @Test
    fun validateMessageTextUsesFirstMeaningfulLine() {
        val text = "# t\n\nMerge branch 'x'\n"
        val r = CommitMessageValidator.validateMessageText(text)
        assertIs<CommitMessageValidator.ValidationResult.Skipped>(r)
    }

    @Test
    fun validateMessageTextMergeSkipped() {
        val r = CommitMessageValidator.validateMessageText("Merge branch 'main' into foo")
        assertIs<CommitMessageValidator.ValidationResult.Skipped>(r)
    }

    @Test
    fun validateMessageTextValidFromMultilineFile() {
        val text = "\n# c\n\ncanales_digitales|canales_2|NOVACOMP|T| ok\n"
        val r = CommitMessageValidator.validateMessageText(text)
        assertIs<CommitMessageValidator.ValidationResult.Ok>(r)
    }

    @Test
    fun formatHintConstantMatchesValidatorCopy() {
        val r = CommitMessageValidator.validate("a|b|c|d")
        assertIs<CommitMessageValidator.ValidationResult.Invalid>(r)
        assert(r.reason.contains(CommitMessageValidator.FORMAT_EXPECTED_HINT))
    }
}
