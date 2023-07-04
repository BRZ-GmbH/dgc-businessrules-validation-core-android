package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import at.gv.brz.brvc.util.isExternalCondition
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * A list of conditions that are linked together with an AND operation
 */
data class AndConditionGroup(
    /**
     * The individual condition names
     */
    val conditions: List<String>
) {
    /**
     * Validates if non-external conditions are available in the provided map.
     *
     * If a condition is not available a SyntaxValidationError.unavailableCondition with the condition name is returned for that condition
     */
    internal fun validate(availableConditions: Map<String, CertificateCondition>): List<BusinessRulesSyntaxError> {
        return conditions.mapNotNull {
            if (it.isExternalCondition() == false) {
                if (availableConditions.containsKey(it) == false) {
                    return@mapNotNull BusinessRulesSyntaxError.UnavailableCondition(it)
                }
            }
            null
        }
    }
}

class AndConditionGroupAdapter {
    @ToJson
    fun toJson(andConditionGroup: AndConditionGroup): List<String> {
        return andConditionGroup.conditions
    }

    @FromJson fun fromJson(conditions: List<String>): AndConditionGroup {
        return AndConditionGroup(conditions)
    }
}