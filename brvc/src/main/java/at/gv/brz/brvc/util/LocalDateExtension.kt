package at.gv.brz.brvc.util

import at.gv.brz.brvc.model.data.ValidityTimeModificationUnit
import at.gv.brz.brvc.model.data.ValidityTimeModifier
import java.time.LocalDateTime

fun LocalDateTime.dateOrEarlierDate(date: LocalDateTime?): LocalDateTime {
    if (date == null) {
        return this
    }
    if (this.isBefore(date)) {
        return this
    }
    return date
}

fun LocalDateTime.dateByAddingUnitAndValue(unit: ValidityTimeModificationUnit, interval: Int): LocalDateTime {
    when (unit) {
        ValidityTimeModificationUnit.MINUTE -> return this.plusMinutes(interval.toLong())
        ValidityTimeModificationUnit.HOUR -> return this.plusHours(interval.toLong())
        ValidityTimeModificationUnit.DAY -> return this.plusDays(interval.toLong())
        ValidityTimeModificationUnit.MONTH -> return this.plusMonths(interval.toLong())
    }
    return this
}

fun LocalDateTime.startOfDay(): LocalDateTime {
    return this.withHour(0).withMinute(0).withSecond(0)
}

fun LocalDateTime.endOfDay(): LocalDateTime {
    return this.withHour(23).withMinute(59).withSecond(59)
}

fun LocalDateTime.dateByModifyingWith(modifier: ValidityTimeModifier?): LocalDateTime {
    if (modifier == null) {
        return this
    }
    return when (modifier) {
        ValidityTimeModifier.START_OF_DAY -> this.startOfDay()
        ValidityTimeModifier.END_OF_DAY -> this.endOfDay()
        ValidityTimeModifier.START_OF_MONTH -> this.withDayOfMonth(1).startOfDay()
        ValidityTimeModifier.END_OF_MONTH -> this.withDayOfMonth(1).startOfDay().plusMonths(1).minusDays(1).endOfDay()
    }
}