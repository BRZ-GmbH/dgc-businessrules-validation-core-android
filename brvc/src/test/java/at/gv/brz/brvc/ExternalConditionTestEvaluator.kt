package at.gv.brz.brvc

class ExternalConditionTestEvaluator: ExternalConditionEvaluator {

    var evaluationBlock: ((condition: String,
                           parameters: Map<String, String>,
                           ruleId: String,
                           ruleCertificateType: String?,
                           region: String,
                           profile: String,
                           originalCertificateObject: Any?) -> Boolean?)? = null

    override fun evaluateExternalCondition(
        condition: String,
        parameters: Map<String, String>,
        ruleId: String,
        ruleCertificateType: String?,
        region: String,
        profile: String,
        originalCertificateObject: Any?
    ): Boolean? {
        return evaluationBlock?.invoke(condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject)
    }
}