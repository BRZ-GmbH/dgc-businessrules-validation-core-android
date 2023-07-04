package at.gv.brz.brvc

import at.gv.brz.brvc.model.*
import at.gv.brz.brvc.model.data.*
import at.gv.brz.brvc.model.internal.ConditionValidationResult
import java.time.ZonedDateTime

/**
 * Validator that allows performing evaluation of modern business rule format on a given certificate
 */
class BusinessRulesValidator(
    private val businessRules: BusinessRuleContainer,
    private val valueSets: Map<String, List<String>>,
    private val validationClock: ZonedDateTime,
    private val externalConditionEvaluator: ExternalConditionEvaluator?,
    private val externalConditionEvaluationStrategy: ExternalConditionEvaluationStrategy
) {
    companion object {
        const val supportedBusinessRuleSchemaVersion = 1
    }

    /**
     * Evaluates the given certificate according to the business rules
     *
     * @param certificate The certificate to validate
     * @param certificateType the type of the given certificate
     * @param expiration the expiration date of the certificate's signature
     * @param issue the issue date of the certificate's signature
     * @param country the country of issuance of this certificate
     * @param region the region (most likely used for federal states) to evaluate the certificate against
     * @param profiles the list of profiles to evaluate the certificate against. If nil, all defined profiles in the business rules format are evaluated
     * @param originalCertificateObject the original certificate object, only passed along to the ExternalConditionEvaluator
     */
    fun evaluateCertificate(certificate: String, certificateType: BusinessRuleCertificateType, expiration: ZonedDateTime, issue: ZonedDateTime, country: String, region: String, profiles: List<String>? = null, originalCertificateObject: Any): Map<String, ValidationResult> {
        val evaluationResults = mutableMapOf<String, ValidationResult>()
        val profilesToEvaluate = profiles ?: businessRules.profiles.map { it.id }
        profilesToEvaluate.forEach { profile ->
            evaluateCertificate(certificate, certificateType, expiration, issue, country, region, profile, originalCertificateObject)?.let { result ->
                evaluationResults[profile] = result
            }
        }
        return evaluationResults
    }

    /**
     * Evaluates the given certificate according to the business rules for a single profile

     * @param certificate The certificate to validate
     * @param certificateType the type of the given certificate
     * @param expiration the expiration date of the certificate's signature
     * @param issue the issue date of the certificate's signature
     * @param country the country of issuance of this certificate
     * @param region the region (most likely used for federal states) to evaluate the certificate against
     * @param profile the profile to evaluate the certificate against
     * @param originalCertificateObject the original certificate object, only passed along to the ExternalConditionEvaluator
     */
    fun evaluateCertificate(certificate: String, certificateType: BusinessRuleCertificateType, expiration: ZonedDateTime, issue: ZonedDateTime, country: String, region: String, profile: String, originalCertificateObject: Any): ValidationResult? {
        val validator = CertificateValidator(
            originalCertificateObject = originalCertificateObject,
            certificatePayload = certificate,
            certificateType = certificateType,
            certificateIssueDate = issue,
            certificateExpiresDate = expiration,
            countryCode = country,
            valueSets = valueSets,
            validationClock = validationClock,
            region = region,
            profile = profile,
            availableConditions = businessRules.conditions,
            externalConditionEvaluator = externalConditionEvaluator,
            externalConditionEvaluationStrategy = externalConditionEvaluationStrategy
        )

        return evaluateCertificateWithValidator(validator, region, profile)
    }

    private fun evaluateCertificateWithValidator(validator: CertificateValidator, region: String, profile: String, group: String? = null): ValidationResult? {
        for (businessRule in businessRules.rules) {
            if (businessRule.isCompatibleToSupportedSchemaVersion(supportedBusinessRuleSchemaVersion) == false) {
                continue
            }
            if (businessRule.isValidAtValidationClock(validationClock) == false) {
                continue
            }
            if (businessRule.isApplicableForRegion(region) == false) {
                continue
            }
            if (businessRule.certificateType != null && businessRule.certificateType != validator.certificateType) {
                continue
            }
            val certificateTypeConditions = validator.evaluateConditions(businessRule.certificateTypeConditions.conditions, businessRule.id, businessRule.certificateTypeString)
            if (certificateTypeConditions.all { it is ConditionValidationResult.Fulfilled } == false) {
                continue
            }
            val evaluatedGeneralConditions = validator.evaluateConditions(businessRule.generalConditions?.conditions ?: listOf(), businessRule.id, businessRule.certificateTypeString)
            if (evaluatedGeneralConditions.all { it is ConditionValidationResult.Fulfilled } == false) {
                val failed = evaluatedGeneralConditions.mapNotNull { (it as? ConditionValidationResult.Failed)?.condition }
                if (failed.isNotEmpty()) {
                    return ValidationResult.Error(failed)
                }

                val violations = evaluatedGeneralConditions.mapNotNull { (it as? ConditionValidationResult.Violated)?.violation }
                if (violations.isNotEmpty()) {
                    return ValidationResult.Invalid(violations)
                }
            }

            val rulesetForProfile = businessRule.ruleSetsByProfileId[profile] ?: return null

            if (businessRule.usesTargetGroups() && !(rulesetForProfile.ruleSetsByGroupKey.keys.count() == 1 && rulesetForProfile.ruleSetsByGroupKey.keys.first() == "all")) {
                var matchingGroup: String? = group
                if (group == null) {
                    for (entry in businessRule.targetGroupsByGroupId!!) {
                        if (validationConditionsInRuleTargetGroup(entry.value, validator, businessRule.id, businessRule.certificateTypeString).all { it is ConditionValidationResult.Fulfilled }) {
                            matchingGroup = entry.key
                            break
                        }
                    }
                }
                if (matchingGroup == null) {
                    return null
                }
                val ruleSet = rulesetForProfile.ruleSetsByGroupKey[matchingGroup] ?: return null
                return validateRuleSet(ruleSet, validator, region, profile, businessRule.id, businessRule.certificateTypeString)
            } else {
                val ruleSet = rulesetForProfile.ruleSetsByGroupKey["all"] ?: return null
                return validateRuleSet(ruleSet, validator, region, profile, businessRule.id, businessRule.certificateTypeString)
            }
        }
        return null
    }

    private fun validateRuleSet(ruleSet: RuleSet, validator: CertificateValidator, region: String, profile: String, ruleId: String, ruleCertificateType: String?): ValidationResult? {
        if (ruleSet.equalToProfile != null) {
            val result = evaluateCertificateWithValidator(validator, region, ruleSet.equalToProfile)
            if (result is ValidationResult.Valid) {
                val linkedConditionsValidation = validateLinkedConditionsForRuleSet(ruleSet, validator, ruleId, ruleCertificateType)
                if (linkedConditionsValidation != null) {
                    var validFrom = result.result.validFrom
                    var validUntil = result.result.validUntil

                    ruleSet.validFrom?.let {
                        validFrom = validateTimes(it, validator, ruleId, ruleCertificateType)
                    }
                    ruleSet.validUntil?.let {
                        validUntil = validateTimes(it, validator, ruleId, ruleCertificateType)
                    }
                    return ValidationResult.Valid(
                        BusinessRuleValidationResult(
                            profile = profile,
                            region = region,
                            validFrom = validFrom,
                            validUntil = validUntil,
                            matchingLinkedConditions = linkedConditionsValidation.first,
                            violatedOrFailedLinkedConditions = linkedConditionsValidation.second
                        )
                    )
                } else {
                    return result
                }
            } else {
                return result
            }
        } else {
            if (ruleSet.invalid == true) {
                return ValidationResult.Invalid(listOf())
            } else {
                val result = validationConditionsInOrGroup(ruleSet.conditions, validator, ruleId, ruleCertificateType)
                if (result.firstOrNull() is ConditionValidationResult.Fulfilled) {
                    val linkedConditionsValidation = validateLinkedConditionsForRuleSet(ruleSet, validator, ruleId, ruleCertificateType)
                    if (linkedConditionsValidation != null) {
                        return ValidationResult.Valid(
                            BusinessRuleValidationResult(
                                profile = profile,
                                region = region,
                                validFrom = validateTimes(
                                    ruleSet.validFrom,
                                    validator,
                                    ruleId,
                                    ruleCertificateType
                                ),
                                validUntil = validateTimes(
                                    ruleSet.validUntil,
                                    validator,
                                    ruleId,
                                    ruleCertificateType
                                ),
                                matchingLinkedConditions = linkedConditionsValidation.first,
                                violatedOrFailedLinkedConditions = linkedConditionsValidation.second
                            )
                        )
                    } else {
                        return ValidationResult.Valid(
                            BusinessRuleValidationResult(
                                profile = profile,
                                region = region,
                                validFrom = validateTimes(
                                    ruleSet.validFrom,
                                    validator,
                                    ruleId,
                                    ruleCertificateType
                                ),
                                validUntil = validateTimes(
                                    ruleSet.validUntil,
                                    validator,
                                    ruleId,
                                    ruleCertificateType
                                ),
                                matchingLinkedConditions = listOf(),
                                violatedOrFailedLinkedConditions = listOf()
                            )
                        )
                    }
                } else if (result.isEmpty()) {
                    return null
                } else {
                    return ValidationResult.Invalid(result.mapNotNull { (it as? ConditionValidationResult.Violated)?.violation })
                }
            }
        }
    }

    enum class LinkedConditionValidationResult {
        fulfilled,
        violated,
        failed
    }

    private fun validateLinkedConditionsForRuleSet(ruleSet: RuleSet, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): Pair<List<BusinessRuleValidationLinkedConditionResult>, List<BusinessRuleValidationLinkedConditionResult>>? {
        val linkedConditions = ruleSet.linkedConditions ?: return null
        if (linkedConditions.isEmpty()) {
            return null
        }

        val validatedLinkedConditions = validateLinkedConditions(linkedConditions, validator, ruleId, ruleCertificateType)
        val fulfilledLinkedConditions = validatedLinkedConditions.filter { it.second == LinkedConditionValidationResult.fulfilled }.map {
            BusinessRuleValidationLinkedConditionResult(it.first.localizedViolationDescription, it.first.conditions.conditions)
        }
        val failedLinkedConditions = validatedLinkedConditions.filter { it.second != LinkedConditionValidationResult.fulfilled }.map {
            BusinessRuleValidationLinkedConditionResult(it.first.localizedViolationDescription, it.first.conditions.conditions)
        }

        return Pair(fulfilledLinkedConditions, failedLinkedConditions)
    }

    private fun validateLinkedConditions(linkedConditions: List<LinkedCondition>, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): List<Pair<LinkedCondition, LinkedConditionValidationResult>> {
        val validationResults = mutableListOf<Pair<LinkedCondition, LinkedConditionValidationResult>>()
        for (linkedCondition in linkedConditions) {
            val evaluations = validator.evaluateConditions(linkedCondition.conditions.conditions, ruleId, ruleCertificateType)
            if (evaluations.all { it is ConditionValidationResult.Fulfilled }) {
                validationResults.add(Pair(linkedCondition, LinkedConditionValidationResult.fulfilled))
            } else {
                if (evaluations.none { it is ConditionValidationResult.Failed }) {
                    validationResults.add(Pair(linkedCondition, LinkedConditionValidationResult.violated))
                } else {
                    validationResults.add(Pair(linkedCondition, LinkedConditionValidationResult.failed))
                }
            }
        }
        return validationResults
    }

    private fun validationConditionsInRuleTargetGroup(ruleTargetGroup: RuleTargetGroup?, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): List<ConditionValidationResult> {
        if (ruleTargetGroup == null) {
            return listOf(ConditionValidationResult.Fulfilled)
        }
        val violations = mutableListOf<ConditionViolation>()
        val failedConditions = mutableListOf<String>()
        for (andConditionGroup in ruleTargetGroup.conditionGroups) {
            val evaluations = validator.evaluateConditions(andConditionGroup.conditions, ruleId, ruleCertificateType)
            if (evaluations.all { it is ConditionValidationResult.Fulfilled }) {
                return listOf(ConditionValidationResult.Fulfilled)
            } else {
                violations.addAll(evaluations.mapNotNull { (it as? ConditionValidationResult.Violated)?.violation })
                failedConditions.addAll(evaluations.mapNotNull { (it as? ConditionValidationResult.Failed)?.condition })
            }
        }
        return when {
            failedConditions.isNotEmpty() -> {
                failedConditions.map { ConditionValidationResult.Failed(it) }
            }
            violations.isNotEmpty() -> {
                violations.map { ConditionValidationResult.Violated(it) }
            }
            else -> {
                listOf(ConditionValidationResult.Failed(""))
            }
        }
    }

    private fun validationConditionsInOrGroup(orGroup: OrConditionGroup?, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): List<ConditionValidationResult> {
        if (orGroup == null) {
            return listOf(ConditionValidationResult.Fulfilled)
        }
        val violations = mutableListOf<ConditionViolation>()
        val failedConditions = mutableListOf<String>()
        for (andConditionGroup in orGroup.conditionGroups) {
            val evaluations = validator.evaluateConditions(andConditionGroup.conditions, ruleId, ruleCertificateType)
            if (evaluations.all { it is ConditionValidationResult.Fulfilled }) {
                return listOf(ConditionValidationResult.Fulfilled)
            } else {
                violations.addAll(evaluations.mapNotNull { (it as? ConditionValidationResult.Violated)?.violation })
                failedConditions.addAll(evaluations.mapNotNull { (it as? ConditionValidationResult.Failed)?.condition })
            }
        }

        return when {
            failedConditions.isNotEmpty() -> {
                failedConditions.map { ConditionValidationResult.Failed(it) }
            }
            violations.isNotEmpty() -> {
                violations.map { ConditionValidationResult.Violated(it) }
            }
            else -> {
                listOf(ConditionValidationResult.Failed(""))
            }
        }
    }

    private fun validateTimes(times: List<ValidityTime>?, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): List<ValidityTimeResult> {
        if (times == null) {
            return listOf()
        }
        val validityTimes = times.mapNotNull { validateTime(it, validator, ruleId, ruleCertificateType) }
        return validityTimes.sortedWith(Comparator { r1, r2 ->
            if (r1.time.isAfter(r2.time)) {
                -1
            } else {
                1
            }
        })
    }

    private fun validateTime(time: ValidityTime, validator: CertificateValidator, ruleId: String, ruleCertificateType: String?): ValidityTimeResult? {
        if (time.conditions?.conditionGroups?.isNotEmpty() == true) {
            for (andConditionGroup in time.conditions.conditionGroups) {
                if (validator.evaluateConditions(andConditionGroup.conditions, ruleId, ruleCertificateType).all { it is ConditionValidationResult.Fulfilled }) {
                    validator.evaluateValidTimeString(time.value)?.let {
                        return ValidityTimeResult(time = time.dateByModifying(it), format = time.format, conditions = null)
                    }
                }
            }
        } else {
            validator.evaluateValidTimeString(time.value)?.let {
                return ValidityTimeResult(time = time.dateByModifying(it), format = time.format, conditions = null)
            }
        }
        return null
    }
}