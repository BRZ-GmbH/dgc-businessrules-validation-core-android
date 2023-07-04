package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * A list of AndConditionGroup that are linked together with an OR operation
 */
internal data class OrConditionGroup(
    /**
     * The individual condition groups
     */
    val conditionGroups: List<AndConditionGroup>
) {
    /**
     * Validates the AND condition groups for possible SyntaxValidationErrors
     */
    fun validate(availableConditions: Map<String, CertificateCondition>): List<BusinessRulesSyntaxError> {
        return conditionGroups.flatMap { it.validate(availableConditions) }
    }
}

internal class OrConditionGroupAdapter {
    @ToJson
    fun toJson(orConditionGroup: OrConditionGroup): List<AndConditionGroup> {
        return orConditionGroup.conditionGroups
    }

    @FromJson
    fun fromJson(conditionGroups: List<AndConditionGroup>): OrConditionGroup {
        return OrConditionGroup(conditionGroups)
    }
}