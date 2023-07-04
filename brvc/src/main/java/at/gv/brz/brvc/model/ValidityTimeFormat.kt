package at.gv.brz.brvc.model

import com.squareup.moshi.Json

/**
 * Enum for the time format of validity times. Should be used to decide which format to use for convert a ValidityTime to human-readable string
 */
enum class ValidityTimeFormat(val value: String) {
    /**
     * Format as date with time
     */
    @Json(name = "dateTime")
    DATE_TIME("dateTime"),

    /**
     * Format as date only without time
     */
    @Json(name = "date")
    DATE("date")
}