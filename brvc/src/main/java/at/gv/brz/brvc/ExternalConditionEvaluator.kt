package at.gv.brz.brvc

/**
 * Interface for an ExternalConditionEvaluator. If passed to validation, this gets called for all external conditions found during validating rules.
 */
interface ExternalConditionEvaluator {
    /**
     * Evaluate the given external condition with the given parameters. BusinessRulesValidationCore also passes the id and certificateType of the rule from which this external condition was triggered as well as the original certificate object that was passed to the validation.
     */
    fun evaluateExternalCondition(
        condition: String,
        parameters: Map<String, String>,
        ruleId: String,
        ruleCertificateType: String?,
        region: String,
        profile: String,
        originalCertificateObject: Any?
    ): Boolean?
}