package at.gv.brz.brvc.model.data

import com.squareup.moshi.Json

enum class ValidityTimeModificationUnit(val value: String) {
    @Json(name = "minute")
    MINUTE("minute"),
    @Json(name = "hour")
    HOUR("hour"),
    @Json(name = "day")
    DAY("day"),
    @Json(name = "month")
    MONTH("month")
}