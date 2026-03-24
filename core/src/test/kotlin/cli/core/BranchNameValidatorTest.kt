package dev.donmanuel.cli.core

import dev.donmanuel.cli.config.BnDefaults
import kotlin.test.Test
import kotlin.test.assertIs

class BranchNameValidatorTest {

    @Test
    fun releaseValid() {
        val r = BranchNameValidator.validate("release/BNMP_V58-Sprint22.05")
        assertIs<BranchNameValidator.ValidationResult.Ok>(r)
    }

    @Test
    fun featureValid() {
        val r = BranchNameValidator.validate(
            "feature/BNMP_V58-Sprint22.05_DCSTI_${BnDefaults.EMPRESA}_HU-116268",
        )
        assertIs<BranchNameValidator.ValidationResult.Ok>(r)
    }

    @Test
    fun mainSkipped() {
        val r = BranchNameValidator.validate("main")
        assertIs<BranchNameValidator.ValidationResult.Skipped>(r)
    }

    @Test
    fun customBranchSkipped() {
        val r = BranchNameValidator.validate("fix/something")
        assertIs<BranchNameValidator.ValidationResult.Skipped>(r)
    }

    @Test
    fun hotfixInvalidSegments() {
        val r = BranchNameValidator.validate("hotfix/merge-banda3-a-banda1")
        assertIs<BranchNameValidator.ValidationResult.Invalid>(r)
    }

    @Test
    fun hotfixWrongEmpresa() {
        val r = BranchNameValidator.validate("hotfix/BNMP_S_DCSTI_X_E_HU-1")
        assertIs<BranchNameValidator.ValidationResult.Invalid>(r)
    }
}
