package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal data class RuleTargetGroup(
    val conditionGroups: List<AndConditionGroup>
) {
    fun validate(availableConditions: Map<String, CertificateCondition>): List<BusinessRulesSyntaxError> {
        return conditionGroups.flatMap { it.validate(availableConditions) }
    }
}

internal class RuleTargetGroupAdapter {
    @ToJson
    fun toJson(ruleTargetGroup: RuleTargetGroup): List<AndConditionGroup> {
        return ruleTargetGroup.conditionGroups
    }

    @FromJson
    fun fromJson(conditionGroups: List<AndConditionGroup>): RuleTargetGroup {
        return RuleTargetGroup(conditionGroups)
    }
}