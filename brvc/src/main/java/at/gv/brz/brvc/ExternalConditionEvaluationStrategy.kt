package at.gv.brz.brvc

/**
 * Determines which strategy to use if no ExternalConditionEvaluator is passed to the validation or a nil value is returned from the ExternalConditionEvaluator (signaling that the condition could not be evaluated)
 */
enum class ExternalConditionEvaluationStrategy {
    /**
     * Always assume the condition to be evaluated to true
     */
    DEFAULT_TO_TRUE,

    /**
     * Always assume the condition to be evaluated to true
     */
    DEFAULT_TO_FALSE,

    /**
     * Return an error duration validation for this condition. Validation returns a ValidationResult.error with the name of the external condition in failedConditions
     */
    FAIL_CONDITION
}