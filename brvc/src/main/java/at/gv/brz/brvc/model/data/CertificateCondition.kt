package at.gv.brz.brvc.model.data

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CertificateCondition(
    @Json(name = "logic") val logic: String,
    @Json(name = "violation_description") val localizedViolationDescription: Map<String, String>?
){
    val parsedJsonLogic: JsonNode? by lazy {
        val objectMapper = ObjectMapper().apply {
            this.findAndRegisterModules()
            registerModule(JavaTimeModule())
        }
        try {
            return@lazy objectMapper.readValue(logic, JsonNode::class.java)
        } catch (ignored: Exception) {
        }
        return@lazy null
    }
}