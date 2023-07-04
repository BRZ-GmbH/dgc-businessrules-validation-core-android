package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * The container holding the definitions in the Modern Business Rule Format
 */
data class BusinessRuleContainer internal constructor(
    /**
     * The specified profiles
     */
    @Json(name = "profiles") val profiles: List<RuleProfile>,
    @Json(name = "conditions") internal val conditions: Map<String, CertificateCondition>,
    @Json(name = "rules") internal val rules: List<BusinessRule>
) {
    companion object {
        /**
         * Parses a BusinessRuleContainer from the given Data. Throws in case the syntax is wrong or the data cannot be parsed for any other reason.
         */
        fun fromData(string: String): BusinessRuleContainer? {
            return Moshi.Builder()
                .add(AndConditionGroupAdapter())
                .add(OrConditionGroupAdapter())
                .add(RuleTargetGroupAdapter())
                .add(ProfileRuleSetAdapter()).addLast(
                    KotlinJsonAdapterFactory()
                )
                .build().adapter(BusinessRuleContainer::class.java).fromJson(string)
        }
    }

    fun toData(): String {
        return Moshi.Builder()
            .add(AndConditionGroupAdapter())
            .add(OrConditionGroupAdapter())
            .add(RuleTargetGroupAdapter())
            .add(ProfileRuleSetAdapter()).addLast(
                KotlinJsonAdapterFactory()
            )
            .build().adapter(BusinessRuleContainer::class.java).toJson(this)
    }

    /**
     * Validates the contents of the BusinessRuleContainer to check for BusinessRulesSyntaxErrors.
     *
     * With the exception of external conditions this validates all conditions and rules in the container for any undefined conditions, profiles or groups, mismatches in the rules or illegal constructs within the rules
     */
    fun validate(): List<BusinessRulesSyntaxError> {
        return rules.flatMap { it.validate(availableConditions = conditions, availableProfiles = profiles) }
    }
}