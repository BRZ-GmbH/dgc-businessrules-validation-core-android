package at.gv.brz.brvc.model

import java.time.LocalDateTime
import java.util.*

/**
 * Holds the validity result
 */
data class ValidityTimeResult(
    /**
     * The date of the validity
     */
    val time: LocalDateTime,
    /**
     * The format
     */
    val format: ValidityTimeFormat,
    /**
     * The conditions which apply to this validity time result or nil if no conditions apply
     */
    val conditions: List<String>?
) {
    override fun equals(other: Any?): Boolean {
        if (other is ValidityTimeResult) {
            return time == other.time &&
                    format == other.format &&
                    (conditions ?: listOf()).size == (other.conditions ?: listOf()).size &&
                    (conditions ?: listOf()).zip((other.conditions ?: listOf())).all { pair -> pair.first == pair.second }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(time, format, (conditions ?: listOf<String>()))
    }
}