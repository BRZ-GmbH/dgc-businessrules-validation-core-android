package at.gv.brz.brvc.model.data

import com.squareup.moshi.Json

/**
 * Enum for the time modifier for a date
 */
enum class ValidityTimeModifier(val value: String) {
    /**
     * Sets the time of a given date to the start of the day (hour, minute, second = 0)
     */
    @Json(name = "startOfDay")
    START_OF_DAY("startOfDay"),

    /**
     * Sets the time of a given date to the end of the day (hour 23, minute 59, second 59)
     */
    @Json(name = "endOfDay")
    END_OF_DAY("endOfDay"),

    /**
     * Sets the time of a given date to the start of the month (and start of the day)
     */
    @Json(name = "startOfMonth")
    START_OF_MONTH("startOfMonth"),

    /**
     * Sets the time of a given date to the end of month (and end of that day)
     */
    @Json(name = "endOfMonth")
    END_OF_MONTH("endOfMonth")
}