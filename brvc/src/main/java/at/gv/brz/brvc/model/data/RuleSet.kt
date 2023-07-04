package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RuleSet(
    @Json(name = "conditions") val conditions: OrConditionGroup?,
    @Json(name = "valid_from") val validFrom: List<ValidityTime>?,
    @Json(name = "valid_until") val validUntil: List<ValidityTime>?,
    @Json(name = "invalid") val invalid: Boolean?,
    @Json(name = "equal_to_profile") val equalToProfile: String?,
    @Json(name = "linked_conditions") val linkedConditions: List<LinkedCondition>?
) {
    fun validate(availableConditions: Map<String, CertificateCondition>, availableProfiles: List<RuleProfile>): List<BusinessRulesSyntaxError> {
        val conditionErrors = conditions?.validate(availableConditions) ?: listOf()
        val linkedConditionErrors = linkedConditions?.flatMap { it.validate(availableConditions) } ?: listOf()
        val profileErrors = if (equalToProfile != null && availableProfiles.firstOrNull { it.id == equalToProfile } == null) listOf(BusinessRulesSyntaxError.UnavailableProfile(equalToProfile)) else listOf()
        val validFromErrors = validFrom?.flatMap { it.validate(availableConditions) } ?: listOf()
        val validUntilErrors = validUntil?.flatMap { it.validate(availableConditions) } ?: listOf()
        return conditionErrors + linkedConditionErrors + profileErrors + validFromErrors + validUntilErrors
    }
}