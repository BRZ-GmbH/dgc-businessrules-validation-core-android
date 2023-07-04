package at.gv.brz.brvc

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import at.gv.brz.brvc.model.ValidityTimeFormat
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ParseTests: BusinessRulesTest() {

    @Test
    fun testParsingOfSimplePayload() {
        val parsedPayload = getBusinessRules("simple")!!
        assertEquals(2, parsedPayload.profiles.size)
        assertEquals("Entry", parsedPayload.profiles.first().id)
        assertEquals("Eintritt", parsedPayload.profiles.first().localizedName["de"])
        assertEquals("Entry", parsedPayload.profiles.first().localizedName["en"])

        assertEquals(26, parsedPayload.conditions.size)
        val sampleCondition = parsedPayload.conditions["isNegativeTestResult"]
        assertNotNull(sampleCondition)
        assertNotNull(sampleCondition?.logic)
        assertEquals("Testresultat ist positiv", sampleCondition?.localizedViolationDescription?.get("de"))
        assertEquals("Test result is positive", sampleCondition?.localizedViolationDescription?.get("en"))
        assertNotNull(sampleCondition?.parsedJsonLogic)
        parsedPayload.conditions.values.forEach { condition ->
            assertNotNull(condition.logic)
            assertNotNull(condition.parsedJsonLogic)
        }
        assertEquals(0, parsedPayload.validate().size)
    }

    @Test
    fun testParsingOfFullPayload() {
        val parsedPayload = getBusinessRules("full")
        assertEquals(8, parsedPayload!!.profiles.count())

        assertEquals("Entry", parsedPayload.profiles.first().id)
        assertEquals("Eintritt", parsedPayload.profiles.first().localizedName["de"])
        assertEquals("Entry", parsedPayload.profiles.first().localizedName["en"])

        assertEquals(26, parsedPayload.conditions.size)
        val sampleCondition = parsedPayload.conditions["isNegativeTestResult"]
        assertNotNull(sampleCondition)
        assertNotNull(sampleCondition?.logic)
        assertEquals(
            "Testresultat ist positiv",
            sampleCondition?.localizedViolationDescription?.get("de")
        )
        assertEquals(
            "Test result is positive",
            sampleCondition?.localizedViolationDescription?.get("en")
        )
        assertNotNull(sampleCondition?.parsedJsonLogic)
        parsedPayload.conditions.values.forEach { condition ->
            assertNotNull(condition.logic)
            assertNotNull(condition.parsedJsonLogic)
        }

        assertEquals(listOf<BusinessRulesSyntaxError>(), parsedPayload.validate())
    }

    @Test
    fun testParsingOfSimpleVaccinationRules() {
        val parsedPayload = getBusinessRules("simple_vaccination")
        assertEquals(2, parsedPayload!!.profiles.count())
        assertEquals(26, parsedPayload.conditions.size)
        assertEquals(listOf<BusinessRulesSyntaxError>(), parsedPayload.validate())
        assertEquals(1, parsedPayload.rules.count())
    }

    @Test
    fun testParsingAndDeserializationOfFullPayload() {
        val parsedPayload = getBusinessRules("simple_vaccination")
        assertNotNull(parsedPayload!!.toData())
    }

    @Test
    fun testParsingOfValidityTime() {
        val parsedPayload = getBusinessRules("parsing_validitytime")
        val ruleSet = parsedPayload?.rules?.firstOrNull()?.ruleSetsByProfileId?.get("Entry")?.ruleSetsByGroupKey?.get("all")
        assertNotNull(ruleSet)
        assertNotNull(ruleSet!!.validUntil)
        val validUntilList = ruleSet.validUntil
        assertEquals(4, validUntilList!!.count())
        assertEquals(ValidityTimeFormat.DATE_TIME, validUntilList[0].format)
        assertEquals(ValidityTimeFormat.DATE_TIME, validUntilList[1].format)
        assertEquals(ValidityTimeFormat.DATE_TIME, validUntilList[2].format)
        assertEquals(ValidityTimeFormat.DATE, validUntilList[3].format)
    }

    @Test
    fun testParsingErrorReservedGroupName() {
        val parsedPayload = getBusinessRules("validation_reserved_group_name")
        val errors = parsedPayload!!.validate()
        assertEquals(1, errors.count())
        assertEquals(BusinessRulesSyntaxError.ReservedTargetGroupName("all"), errors.first())
    }

    @Test
    fun testParsingWithoutRuleValidity() {
        val parsedPayload = getBusinessRules("parsing_without_rule_validity")
        assertTrue(parsedPayload?.rules?.first()?.validUntil?.isEqualToTheDayWithDate(LocalDateTime.MAX)!!)
        assertTrue(parsedPayload.rules.first().validFrom.isEqualToTheDayWithDate(LocalDateTime.MIN))
    }
}

fun LocalDateTime.modified(years: Long = 0, days: Long = 0, hours: Long = 0, minutes: Long = 0): LocalDateTime {
    return this.plusMinutes(minutes).plusHours(hours).plusDays(days).plusYears(years)
}

fun LocalDateTime.isEqualToTheDayWithDate(date: LocalDateTime): Boolean {
    return this.truncatedTo(ChronoUnit.DAYS).isEqual(date.truncatedTo(ChronoUnit.DAYS))
}

fun LocalDateTime.isEqualToTheMinuteWithDate(date: LocalDateTime): Boolean {
    return this.truncatedTo(ChronoUnit.MINUTES).isEqual(date.truncatedTo(ChronoUnit.MINUTES))
}
