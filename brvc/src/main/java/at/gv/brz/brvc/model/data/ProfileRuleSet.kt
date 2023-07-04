package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

internal data class ProfileRuleSet(
    val ruleSetsByGroupKey: Map<String, RuleSet>
) {
    fun validate(availableConditions: Map<String, CertificateCondition>, availableProfiles: List<RuleProfile>, availableTargetGroups: Map<String, RuleTargetGroup>?): List<BusinessRulesSyntaxError> {
        val errors = mutableListOf<BusinessRulesSyntaxError>()
        errors.addAll(ruleSetsByGroupKey.values.flatMap { it.validate(availableConditions, availableProfiles) })
        errors.addAll(ruleSetsByGroupKey.keys.mapNotNull { if (it != "all" && availableTargetGroups?.containsKey(it) == false) BusinessRulesSyntaxError.UnavailableTargetGroup(it) else null })
        return errors
    }
}

internal class ProfileRuleSetAdapter {
    @ToJson
    fun toJson(profileRuleSet: ProfileRuleSet): Map<String, RuleSet> {
        return profileRuleSet.ruleSetsByGroupKey
    }

    @FromJson
    fun fromJson(ruleSets: Map<String, RuleSet>): ProfileRuleSet {
        return ProfileRuleSet(ruleSets)
    }
}