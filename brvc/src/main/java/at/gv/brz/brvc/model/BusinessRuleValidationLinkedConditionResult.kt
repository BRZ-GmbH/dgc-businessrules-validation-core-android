package at.gv.brz.brvc.model

/**
 * Represent the validation result of linked conditions
 */
data class BusinessRuleValidationLinkedConditionResult(
    /**
     * The localized violation message for this condition that provides a user-readable description of why the linked condition failed
     */
    val violationMessage: Map<String, String>?,
    /**
     * List of condition names that were evaluated with this linked condition
     */
    val conditions: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (other is BusinessRuleValidationLinkedConditionResult) {
            return other.conditions.size == conditions.size && conditions.zip(other.conditions).all { pair -> pair.first == pair.second }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return conditions.hashCode()
    }
}