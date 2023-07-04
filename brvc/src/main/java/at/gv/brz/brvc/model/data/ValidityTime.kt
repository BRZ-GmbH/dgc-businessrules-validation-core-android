package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import at.gv.brz.brvc.model.ValidityTimeFormat
import at.gv.brz.brvc.util.dateByAddingUnitAndValue
import at.gv.brz.brvc.util.dateByModifyingWith
import at.gv.brz.brvc.util.dateOrEarlierDate
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
internal data class ValidityTime internal constructor(
    @Json(name = "conditions") val conditions: OrConditionGroup?,
    @Json(name = "value") val value: String,
    @Json(name = "max") val maxDateString: String?,
    @Json(name = "plus_unit") val unitString: String?,
    @Json(name = "plus_interval") val interval: Int?,
    @Json(name = "format") val formatString: String?,
    @Json(name = "modifier") val modifierString: String?
) {

    val maxDate: LocalDateTime?
        get() {
            if (maxDateString == null) {
                return null
            }
            try {
                return LocalDateTime.parse(maxDateString, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                // Ignore exception when date string cannot be parsed
            }
            return null
        }

    val format: ValidityTimeFormat
        get() {
            if (formatString == null) {
                return ValidityTimeFormat.DATE_TIME
            }
            return ValidityTimeFormat.values().firstOrNull { it.value == formatString } ?: ValidityTimeFormat.DATE_TIME
        }

    val modifier: ValidityTimeModifier?
        get() {
            if (modifierString == null) {
                return null
            }
            return ValidityTimeModifier.values().firstOrNull { it.value == modifierString }
        }

    val unit: ValidityTimeModificationUnit?
        get() {
            if (unitString == null) {
                return null
            }
            return ValidityTimeModificationUnit.values().firstOrNull { it.value == unitString }
        }

    fun dateByModifying(date: LocalDateTime): LocalDateTime {
        val modificationUnit = unit
        if (modificationUnit == null || interval == null) {
            return date.dateOrEarlierDate(maxDate)
        }
        return date
            .dateByAddingUnitAndValue(modificationUnit, interval)
            .dateByModifyingWith(modifier)
            .dateOrEarlierDate(maxDate)
    }

    internal fun validate(availableConditions: Map<String, CertificateCondition>): List<BusinessRulesSyntaxError> {
        return conditions?.validate(availableConditions) ?: listOf()
    }
}
