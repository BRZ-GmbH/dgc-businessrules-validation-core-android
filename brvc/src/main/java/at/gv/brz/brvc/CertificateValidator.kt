package at.gv.brz.brvc

import at.gv.brz.brvc.model.ConditionViolation
import at.gv.brz.brvc.model.data.BusinessRuleCertificateType
import at.gv.brz.brvc.model.data.CertificateCondition
import at.gv.brz.brvc.model.internal.ConditionValidationResult
import at.gv.brz.brvc.util.externalConditionNameAndArguments
import at.gv.brz.brvc.util.isExternalCondition
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import eu.ehn.dcc.certlogic.evaluate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class CertificateValidator(
    val originalCertificateObject: Any?,
    certificatePayload: String,
    val certificateType: BusinessRuleCertificateType,
    certificateIssueDate: ZonedDateTime,
    certificateExpiresDate: ZonedDateTime,
    countryCode: String,
    valueSets: Map<String, List<String>>,
    validationClock: ZonedDateTime,
    val region: String,
    val profile: String,
    val availableConditions: Map<String, CertificateCondition>,
    val externalConditionEvaluator: ExternalConditionEvaluator?,
    val externalConditionEvaluationStrategy: ExternalConditionEvaluationStrategy
) {
    val jsonObjectForValidation: ObjectNode?

    private val objectMapper: ObjectMapper by lazy {
        ObjectMapper().apply {
            this.findAndRegisterModules()
            registerModule(JavaTimeModule())
        }
    }

    init {
        val externalParameter = BusinessRulesCoreHelper.getExternalParameterStringForValidation(objectMapper, valueSets, validationClock, certificateIssueDate, certificateExpiresDate)
        jsonObjectForValidation = BusinessRulesCoreHelper.getJsonObjectForValidation(objectMapper, certificatePayload, externalParameter)
    }

    fun evaluateValidTimeString(evaluationString: String): LocalDateTime? {
        try {
            if (evaluationString.startsWith("#") && evaluationString.endsWith("#")) {
                val placeholderValue = evaluatePlaceholderSubstitution(evaluationString.replace("#", ""))
                if (placeholderValue != null && placeholderValue is NullNode == false) {
                    placeholderValue.asText()?.let {
                        try {
                            return LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                        } catch (e: Exception) {
                            try {
                                return LocalDate.parse(
                                    it,
                                    DateTimeFormatter.ISO_DATE
                                ).atTime(LocalTime.now())
                            } catch (e: Exception) {
                            }
                        }
                    }
                }
            } else {
                return LocalDateTime.parse(evaluationString, DateTimeFormatter.ISO_DATE_TIME)
            }
        } catch(e: Exception) {
        }
        return null
    }

    private fun evaluatePlaceholderSubstitution(variablePath: String): JsonNode? {
        if (jsonObjectForValidation == null) {
            return null
        }
        try {
            val evaluationPath =
                objectMapper.readTree("{\"var\": \"${variablePath}\"}")
            return evaluate(evaluationPath, jsonObjectForValidation)
        } catch(e: Exception) {}
        return null
    }

    fun evaluateConditions(conditions: List<String>, ruleId: String, ruleCertificateType: String?): List<ConditionValidationResult> {
        return conditions.map { evaluateCondition(it, ruleId, ruleCertificateType) }
    }

    private fun evaluateCondition(conditionName: String, ruleId: String, ruleCertificateType: String?): ConditionValidationResult {
        if (conditionName.isExternalCondition()) {
            val conditionNameAndArguments = conditionName.externalConditionNameAndArguments() ?: return ConditionValidationResult.Failed(conditionName)
            val result = externalConditionEvaluator?.evaluateExternalCondition(conditionNameAndArguments.condition, parameters = conditionNameAndArguments.parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject)
            if (result != null) {
                if (result) {
                    return ConditionValidationResult.Fulfilled
                } else {
                    return ConditionValidationResult.Violated(ConditionViolation(conditionName, null))
                }
            } else {
                when (externalConditionEvaluationStrategy) {
                    ExternalConditionEvaluationStrategy.DEFAULT_TO_TRUE -> return ConditionValidationResult.Fulfilled
                    ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE -> return ConditionValidationResult.Violated(
                        ConditionViolation(conditionName, null)
                    )
                    ExternalConditionEvaluationStrategy.FAIL_CONDITION -> return ConditionValidationResult.Failed(conditionName)
                }
            }
        } else {
            val condition = availableConditions[conditionName] ?: return ConditionValidationResult.Failed(conditionName)
            val result = evaluateBooleanRule(condition.parsedJsonLogic) ?: return ConditionValidationResult.Failed(conditionName)
            if (result) {
                return ConditionValidationResult.Fulfilled
            } else {
                return ConditionValidationResult.Violated(violation = ConditionViolation(condition = conditionName, message = condition.localizedViolationDescription))
            }
        }
    }

    /**
     * Evaluates the given JSONLogic rule on the passed validation object. If the rule does not evaluate to a boolean or throws an error, nil is returned
     */
    private fun evaluateBooleanRule(rule: JsonNode?): Boolean? {
        if (rule == null || jsonObjectForValidation == null) {
            return null
        }
        return BusinessRulesCoreHelper.evaluateBooleanRule(rule, jsonObjectForValidation)
    }

}