package at.gv.brz.brvc.model.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RegionCondition(
    @Json(name = "include") val include: List<String>?,
    @Json(name = "exclude") val exclude: List<String>?
)