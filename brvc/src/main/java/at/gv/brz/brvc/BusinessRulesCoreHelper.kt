package at.gv.brz.brvc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import eu.ehn.dcc.certlogic.evaluate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BusinessRulesCoreHelper {
    companion object {

        val DEFAULT_DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        /**
         * Returns a JSON object encoding the given EU Health certificate payload and external parameters
         */
        fun getJsonObjectForValidation(objectMapper: ObjectMapper, certificatePayload: String, externalParameterString: String): ObjectNode {
            return getJsonNodeForValidation(objectMapper, externalParameterString, certificatePayload)
        }

        private fun getJsonNodeForValidation(objectMapper: ObjectMapper, externalParameterString: String, payload: String): ObjectNode {
            return objectMapper.createObjectNode().apply {
                this.set<JsonNode>(
                    "external",
                    objectMapper.readValue(externalParameterString)
                )
                this.set<JsonNode>(
                    "payload",
                    objectMapper.readValue<JsonNode>(payload)
                )
            }
        }

        /**
         * Encodes the given external parameters to the appropriate parameter string for JSONLogic rule validation
         */
        fun getExternalParameterStringForValidation(objectMapper: ObjectMapper, valueSets: Map<String, List<String>>, validationClock: ZonedDateTime, issuedAt: ZonedDateTime, expiresAt: ZonedDateTime): String {
            val externalParameter = mapOf("validationClock" to DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(validationClock), "valueSets" to valueSets, "countryCode" to "AT", "exp" to expiresAt, "iat" to issuedAt, "issuerCountryCode" to "AT", "kid" to "", "region" to "")

            return objectMapper.writeValueAsString(externalParameter)
        }

        /**
         * Evaluates the given JSONLogic rule on the passed validation object. If the rule does not evaluate to a boolean or throws an error, null is returned
         */
        fun evaluateBooleanRule(rule: JsonNode, objectForValidation: ObjectNode): Boolean? {
            try {
                val result = evaluate(rule, objectForValidation) as? BooleanNode ?: return null
                return result.asBoolean()
            } catch (e: Exception){}

            return null
        }

        /**
         * Searches for placeholders (contained between two hash symbols - e.g. #name#) in the given string and evaluates the referenced path in the given JSON object to replace them with actual values or an empty string if they are not successfully evaluated
         */
        fun evaluatePlaceholdersInString(string: String, objectForValidation: ObjectNode, objectMapper: ObjectMapper): String {
            var evaluationString = string
            val regularExpression = "#[^#]*#".toRegex()
            val matchResult = regularExpression.findAll(evaluationString)
            for (placeholder in matchResult) {
                try {
                    val evaluationPath =
                        objectMapper.readTree("{\"var\": \"${placeholder.value.replace("#", "")}\"}")
                    val evaluatedValue = evaluate(evaluationPath, objectForValidation)
                    if (evaluatedValue is NullNode) {
                        evaluationString =
                            evaluationString.replaceFirst(oldValue = placeholder.value, newValue = "")
                    } else {
                        val evaluatedStringValue = evaluatedValue.asText()
                        try {
                            val date = LocalDate.parse(evaluatedStringValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            evaluationString =
                                evaluationString.replaceFirst(oldValue = placeholder.value, newValue = DEFAULT_DISPLAY_DATE_FORMATTER.format(date))
                        } catch (exception: Exception) {
                            evaluationString =
                                evaluationString.replaceFirst(oldValue = placeholder.value, newValue = evaluatedStringValue)
                        }
                    }
                } catch (e: Exception) {
                    evaluationString =
                        evaluationString.replaceFirst(oldValue = placeholder.value, newValue = "")
                }
            }
            return evaluationString
        }
    }
}