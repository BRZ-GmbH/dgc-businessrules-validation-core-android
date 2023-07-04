package at.gv.brz.brvc.model.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RuleProfile(
    @Json(name = "id") val id: String,
    @Json(name = "name") val localizedName: Map<String, String>,
    @Json(name = "links") val links: Map<String, String>?
)