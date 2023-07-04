package at.gv.brz.brvc.model

/**
 * Represents the validation result from BusinessRulesValidationCore for a certificate and profile
 */
sealed class ValidationResult {
    /**
     * The certificate was evaluated successfully and is valid for the evaluated region and profile
     */
    data class Valid(val result: BusinessRuleValidationResult): ValidationResult()

    /**
     * The certificate was evaluated successfully but is not valid for the evaluated region and profile. The list of violations contains ALL conditions that the certificate failed.
     */
    data class Invalid(val violations: List<ConditionViolation>): ValidationResult()

    /**
     * The certificate was not evaluated successfully because the evaluation of certain conditions failed. The list of failedConditions contains ALL conditions that led to an error.
     */
    data class Error(val failedConditions: List<String>): ValidationResult()

    override fun equals(other: Any?): Boolean {
        if (this is Valid && other is Valid) {
            return result == other.result
        } else if (this is Invalid && other is Invalid) {
            return violations.size == other.violations.size &&
                    violations.zip(other.violations).all { pair -> pair.first == pair.second }
        } else if (this is Error && other is Error) {
            return failedConditions.size == other.failedConditions.size &&
                    failedConditions.zip(other.failedConditions).all { pair -> pair.first == pair.second }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        if (this is Valid) {
            return result.hashCode()
        } else if (this is Invalid) {
            return violations.hashCode()
        } else if (this is Error) {
            return failedConditions.hashCode()
        }
        return super.hashCode()
    }
}