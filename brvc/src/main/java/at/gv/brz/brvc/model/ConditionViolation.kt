package at.gv.brz.brvc.model

/**
 * Wraps the violation of an individual condition
 */
data class ConditionViolation(
    /**
     * The identifier for the condition that was violated
     */
    val condition: String,
    /**
     * The localized violation message for this condition that provides a user-readable description of the reason why this condition was violated
     */
    val message: Map<String, String>?
) {
    override fun equals(other: Any?): Boolean {
        if (other is ConditionViolation) {
            return other.condition == condition
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return condition.hashCode()
    }
}