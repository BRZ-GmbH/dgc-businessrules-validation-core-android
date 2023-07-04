package at.gv.brz.brvc

import at.gv.brz.brvc.model.data.ValidityTimeModificationUnit
import at.gv.brz.brvc.model.data.ValidityTimeModifier
import at.gv.brz.brvc.util.dateByAddingUnitAndValue
import at.gv.brz.brvc.util.dateByModifyingWith
import at.gv.brz.brvc.util.dateOrEarlierDate
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateExtensionTests {

    @Test
    fun testComparisons() {
        val date1 = LocalDateTime.now()
        val date2 = LocalDateTime.now().plusSeconds(10)

        assertEquals(date1, date1.dateOrEarlierDate(null))
        assertEquals(date1, date1.dateOrEarlierDate(date2))
        assertEquals(date1, date2.dateOrEarlierDate(date1))
    }

    @Test
    fun testDateModifications() {
        assertTrue(LocalDateTime.now().dateByAddingUnitAndValue(ValidityTimeModificationUnit.MINUTE, 10).isEqualToTheMinuteWithDate(LocalDateTime.now().plusMinutes(10)))
        assertTrue(LocalDateTime.now().dateByAddingUnitAndValue(ValidityTimeModificationUnit.HOUR, 10).isEqualToTheMinuteWithDate(LocalDateTime.now().plusHours(10)))
        assertTrue(LocalDateTime.now().dateByAddingUnitAndValue(ValidityTimeModificationUnit.DAY, 2).isEqualToTheMinuteWithDate(LocalDateTime.now().plusDays(2)))
        assertTrue(LocalDateTime.now().dateByAddingUnitAndValue(ValidityTimeModificationUnit.MONTH, 5).isEqualToTheMinuteWithDate(LocalDateTime.now().plusMonths(5)))
    }

    @Test
    fun testDateModifiers() {
        val date = LocalDateTime.parse("2022-08-03T17:42:00Z", DateTimeFormatter.ISO_DATE_TIME)

        val startOfDay = date.dateByModifyingWith(ValidityTimeModifier.START_OF_DAY)
        assertEquals(3, startOfDay.dayOfMonth)
        assertEquals(8, startOfDay.monthValue)
        assertEquals(0, startOfDay.hour)
        assertEquals(0, startOfDay.minute)

        val endOfDay = date.dateByModifyingWith(ValidityTimeModifier.END_OF_DAY)
        assertEquals(3, endOfDay.dayOfMonth)
        assertEquals(8, endOfDay.monthValue)
        assertEquals(23, endOfDay.hour)
        assertEquals(59, endOfDay.minute)

        val startOfMonth = date.dateByModifyingWith(ValidityTimeModifier.START_OF_MONTH)
        assertEquals(1, startOfMonth.dayOfMonth)
        assertEquals(8, startOfMonth.monthValue)
        assertEquals(0, startOfMonth.hour)
        assertEquals(0, startOfMonth.minute)

        val endOfMonth = date.dateByModifyingWith(ValidityTimeModifier.END_OF_MONTH)
        assertEquals(31, endOfMonth.dayOfMonth)
        assertEquals(8, endOfMonth.monthValue)
        assertEquals(23, endOfMonth.hour)
        assertEquals(59, endOfMonth.minute)
    }
}