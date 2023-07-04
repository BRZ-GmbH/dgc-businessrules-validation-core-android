package at.gv.brz.brvc.util

/**
 * Returns whether the string starts with the prefix for an external condition
 */
fun String.isExternalCondition(): Boolean {
    return startsWith("ext.")
}

internal fun String.subStringToFirstNonLetter(): String {
    val firstNonLetterIndex = this.indexOfFirst { !it.isLetter() }
    if (firstNonLetterIndex != -1) {
        return this.substring(0, firstNonLetterIndex)
    }
    return this
}

/**
 * Wrapper class containing the parsed condition name and parameters for an external condition
 */
data class ExternalConditionNameAndArguments(
    val condition: String,
    val parameters: Map<String, String>
)

/**
 *  Returns a normalized identifier derived from the familyName, givenName and date of birth of a digital green certificate. This can be used to determine if different certificate likely belong to the same person despite slight differences in the name on the certificate (most-like differences due to multiple given names).
 *
 *  This method clips the familyName and givenName at the first non-letter character and concatenated a lowercased version together with the date of birth.
 *
 *  It is recommended to use the standardized familyName and givenName values of the certificate (fields fnt and gnt)
 */
fun String.Companion.personGroupingIdentifierForDGCCertificate(familyName: String?, givenName: String?, dateOfBirth: String?): String {
    val normalizedFamilyName = (familyName ?: "").lowercase().subStringToFirstNonLetter()
    val normalizedGivenName = (givenName ?: "").lowercase().subStringToFirstNonLetter()
    val normalizedDateOfBirth = dateOfBirth ?: ""
    return "${normalizedFamilyName}_${normalizedGivenName}_${normalizedDateOfBirth}"
}

/**
 * Returns the parsed external condition name and parameters
 */
fun String.externalConditionNameAndArguments(): ExternalConditionNameAndArguments? {
    if (isExternalCondition() == false) {
        return null
    }

    val conditionNameAndArguments = drop(4).split("__").filter { it.isNotEmpty() }
    val name = conditionNameAndArguments.firstOrNull() ?: return null

    val arguments = conditionNameAndArguments.drop(1)
    val parameters = arguments.mapNotNull { argument ->
        val parameter = argument.split(":")
        if (parameter.size == 2) {
            val parameterName = parameter.firstOrNull() ?: return@mapNotNull null
            val parameterValue = parameter.lastOrNull() ?: return@mapNotNull null
            if (parameterName.isNotEmpty() && parameterValue.isNotEmpty()) {
                return@mapNotNull parameterName to parameterValue
            }
        }
        return@mapNotNull null
    }.toMap()

    return ExternalConditionNameAndArguments(name, parameters)
}