package at.gv.brz.brvc.model

import java.util.*

/**
 * Represents the valid validation result of a certificate for a single region and profile
 */
data class BusinessRuleValidationResult(
    /**
     * The profile for which the certificate is valid
     */
    val profile: String,
    /**
     * The region for which the certificate is valid
     */
    val region: String,
    /**
     * Time results from when this certificate is valid. Sorted ascending with the first entry holding the "earliest possible date". Each ValidityTimeResult might hold conditions when it applies.
     */
    val validFrom: List<ValidityTimeResult>,
    /**
     * Time results until when this certificate is valid. Sorted descending with the first entry being the "latest possible date". Each ValidityTimeResult might hold conditions when it applies.
     */
    val validUntil: List<ValidityTimeResult>,
    /**
     * List of linked conditions that were successfully evaluated to determine the validity of this certificate
     */
    val matchingLinkedConditions: List<BusinessRuleValidationLinkedConditionResult>,
    /**
     * List of linked conditions that were violated while determining the validity of this certificate
     */
    val violatedOrFailedLinkedConditions: List<BusinessRuleValidationLinkedConditionResult>
) {
    override fun equals(other: Any?): Boolean {
        if (other is BusinessRuleValidationResult) {
            return profile == other.profile &&
                    region == other.region &&
                    validFrom.size == other.validFrom.size &&
                    validFrom.zip(other.validFrom).all { pair -> pair.first == pair.second } &&
                    validUntil.size == other.validUntil.size &&
                    validUntil.zip(other.validUntil).all { pair -> pair.first == pair.second } &&
                    matchingLinkedConditions.size == other.matchingLinkedConditions.size &&
                    matchingLinkedConditions.zip(other.matchingLinkedConditions).all { pair -> pair.first == pair.second } &&
                    violatedOrFailedLinkedConditions.size == other.violatedOrFailedLinkedConditions.size &&
                    violatedOrFailedLinkedConditions.zip(other.violatedOrFailedLinkedConditions).all { pair -> pair.first == pair.second }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(profile, region, validFrom, validUntil, matchingLinkedConditions, violatedOrFailedLinkedConditions)
    }
}