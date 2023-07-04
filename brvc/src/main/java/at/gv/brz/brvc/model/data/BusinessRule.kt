package at.gv.brz.brvc.model.data

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import com.squareup.moshi.Json
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal data class BusinessRule(
    @Json(name = "id") val id: String,
    @Json(name = "schema_version") val schemaVersion: Int,
    @Json(name = "regions") val regionCondition: RegionCondition,
    @Json(name = "certificate_type") val certificateTypeString: String?,
    @Json(name = "certificate_type_conditions") val certificateTypeConditions: AndConditionGroup,
    @Json(name = "valid_from") val validFromString: String?,
    @Json(name = "valid_until") val validUntilString: String?,
    @Json(name = "general_conditions") val generalConditions: AndConditionGroup?,
    @Json(name = "profiles") val ruleSetsByProfileId: Map<String, ProfileRuleSet>,
    @Json(name = "groups") val targetGroupsByGroupId: Map<String, RuleTargetGroup>?
) {
    val validFrom: LocalDateTime
        get() {
            if (validFromString == null) {
                return LocalDateTime.MIN
            }
            try {
                return LocalDateTime.parse(validFromString, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                // Ignore exception when date string cannot be parsed
            }
            return LocalDateTime.MIN
        }

    val validUntil: LocalDateTime
        get() {
            if (validUntilString == null) {
                return LocalDateTime.MAX
            }
            try {
                return LocalDateTime.parse(validUntilString, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                // Ignore exception when date string cannot be parsed
            }
            return LocalDateTime.MAX
        }

    val certificateType: BusinessRuleCertificateType?
        get() {
            if (certificateTypeString == null) {
                return null
            }
            if (certificateTypeString.lowercase() == BusinessRuleCertificateType.VACCINATION.value) {
                return BusinessRuleCertificateType.VACCINATION
            } else if (certificateTypeString.lowercase() == BusinessRuleCertificateType.TEST.value) {
                return BusinessRuleCertificateType.TEST
            } else if (certificateTypeString.lowercase() == BusinessRuleCertificateType.RECOVERY.value) {
                return BusinessRuleCertificateType.RECOVERY
            } else if (certificateTypeString.lowercase() == BusinessRuleCertificateType.VACCINATION_EXEMPTION.value) {
                return BusinessRuleCertificateType.VACCINATION_EXEMPTION
            }
            return null
        }

    fun validate(availableConditions: Map<String, CertificateCondition>, availableProfiles: List<RuleProfile>): List<BusinessRulesSyntaxError> {
        val errors = mutableListOf<BusinessRulesSyntaxError>()
        errors.addAll(generalConditions?.validate(availableConditions) ?: listOf())
        errors.addAll(certificateTypeConditions.validate(availableConditions))
        errors.addAll(targetGroupsByGroupId?.values?.flatMap { it.validate(availableConditions) } ?: listOf())
        if (targetGroupsByGroupId?.containsKey("all") == true) {
            errors.add(BusinessRulesSyntaxError.ReservedTargetGroupName("all"))
        }
        errors.addAll(ruleSetsByProfileId.keys.mapNotNull { profileKey ->
            if (availableProfiles.firstOrNull { it.id  == profileKey} == null) {
                return@mapNotNull BusinessRulesSyntaxError.UnavailableProfile(profileKey)
            }
            return@mapNotNull null
        })
        errors.addAll(ruleSetsByProfileId.values.flatMap { it.validate(availableConditions = availableConditions, availableProfiles = availableProfiles, availableTargetGroups = targetGroupsByGroupId) })

        ruleSetsByProfileId.forEach { profile, profileRuleSet ->
            profileRuleSet.ruleSetsByGroupKey.forEach { group, ruleSet ->
                if (ruleSet.equalToProfile != null) {
                    checkForNonLinkedRuleSetForProfile(ruleSet.equalToProfile, group = group)?.let {
                        errors.add(it)
                    }
                }
            }
        }

        return errors
    }

    fun isCompatibleToSupportedSchemaVersion(supportedSchemaVersion: Int): Boolean {
        return schemaVersion <= supportedSchemaVersion
    }

    fun isApplicableForRegion(region: String): Boolean {
        val includedRegions = regionCondition.include ?: listOf()
        val excludedRegions = regionCondition.exclude ?: listOf()

        return (includedRegions.contains(region) || includedRegions.contains("all")) && excludedRegions.contains(region) == false
    }

    fun isValidAtValidationClock(validationClock: ZonedDateTime): Boolean {
        return validFrom.isBefore(validationClock.toLocalDateTime()) && validUntil.isAfter(validationClock.toLocalDateTime())
    }

    fun usesTargetGroups(): Boolean {
        return (targetGroupsByGroupId ?: mapOf()).isEmpty() == false
    }

    fun checkForNonLinkedRuleSetForProfile(profile: String, group: String): BusinessRulesSyntaxError? {
        val profileRuleSet = ruleSetsByProfileId[profile] ?: return BusinessRulesSyntaxError.UnknownLinkedProfile(profile)
        val ruleSet = profileRuleSet.ruleSetsByGroupKey[group] ?: return BusinessRulesSyntaxError.UnknownTargetGroupInLinkedProfile(profile = profile, targetGroup = group)

        if (ruleSet.equalToProfile != null) {
            return BusinessRulesSyntaxError.UnallowedMultistepProfileChain(profile = profile, targetGroup = group)
        }
        return null
    }

}