package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import at.gv.brz.brvc.util.isExternalCondition
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class LinkedCondition(
    @Json(name = "violation_description") val localizedViolationDescription: Map<String, String>?,
    @Json(name = "conditions") val conditions: AndConditionGroup
) {
    fun validate(availableConditions: Map<String, CertificateCondition>): List<BusinessRulesSyntaxError> {
        return conditions.conditions.mapNotNull {
            if (it.isExternalCondition() == false) {
                if (availableConditions.containsKey(it) == false) {
                    return@mapNotNull BusinessRulesSyntaxError.UnavailableCondition(it)
                }
            }
            return@mapNotNull null
        }
    }
}