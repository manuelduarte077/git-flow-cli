package dev.donmanuel.cli.core

import kotlin.test.Test
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
}
