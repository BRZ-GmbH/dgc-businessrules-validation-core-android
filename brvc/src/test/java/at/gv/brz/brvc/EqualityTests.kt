package at.gv.brz.brvc

import at.gv.brz.brvc.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class EqualityTests {

    @Test
    fun testConditionViolation() {
        assertEquals(ConditionViolation(condition = "condition", message = null), ConditionViolation(condition = "condition", message = null))
        assertEquals(ConditionViolation(condition = "condition", message = null), ConditionViolation(condition = "condition", message = mapOf("de" to "test", "en" to "test")))
        assertEquals(ConditionViolation(condition = "condition", message = mapOf("de" to "test")), ConditionViolation(condition = "condition", message = mapOf("de" to "test", "en" to "test")))
        assertNotEquals(ConditionViolation(condition = "condition", message = mapOf("de" to "test", "en" to "test")), ConditionViolation(condition = "Condition", message = mapOf("de" to "test", "en" to "test")))
    }

    @Test
    fun testBusinessRuleValidationLinkedConditionResult() {
        assertEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")))
        assertEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = null, conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")))
        assertEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = null, conditions = listOf("condition")))
        assertEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf("de" to "test", "en" to "test"), conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = null, conditions = listOf("condition")))

        assertNotEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = null, conditions = listOf("Condition")))
        assertNotEquals(BusinessRuleValidationLinkedConditionResult(violationMessage = mapOf(), conditions = listOf("condition")), BusinessRuleValidationLinkedConditionResult(violationMessage = null, conditions = listOf("condition", "condition2")))
    }

    @Test
    fun testBusinessRuleValidationResult() {
        val time = LocalDateTime.now().toInstant(ZoneOffset.UTC)
        val date = LocalDateTime.ofInstant(time, ZoneOffset.UTC)
        val sameDate = LocalDateTime.ofInstant(time, ZoneOffset.UTC)

        // Check equality check for profile and region
        var result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        var result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "NG", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "NOE", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        // Check equality check for valid from
        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition1"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition2"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(
            ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition2"))
        ), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        // Check equality check for valid until
        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition1"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition2"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("condition"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("condition2"))), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        // Check equality check for matchingLinkedConditions
        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ), violatedOrFailedLinkedConditions = listOf())
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test1"))
        ), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))
        ), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))
        ), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2")), BusinessRuleValidationLinkedConditionResult(null, listOf("Test 3"))
        ), violatedOrFailedLinkedConditions = listOf())
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        // Check equality check for violatedOrFailedLinkedConditions
        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ))
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ))
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test"))
        ))
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test1"))
        ))
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf())
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))
        ))
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))
        ))
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", validFrom = listOf(), validUntil = listOf(), matchingLinkedConditions = listOf(), violatedOrFailedLinkedConditions = listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2")), BusinessRuleValidationLinkedConditionResult(null, listOf("Test 3"))
        ))
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))
    }

    @Test
    fun testValidationResult() {
        assertEquals(ValidationResult.Invalid(violations = listOf()), ValidationResult.Invalid(violations = listOf()))
        assertEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", null))), ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", mapOf("de" to "test")))))
        assertEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", mapOf("de" to "test")))), ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", null))))
        assertEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", mapOf("de" to "test")))), ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", mapOf("de" to "test", "en" to "test")))))

        assertNotEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", null))), ValidationResult.Invalid(violations = listOf(ConditionViolation("condition1", mapOf("de" to "test")))))
        assertNotEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", null))), ValidationResult.Invalid(violations = listOf(ConditionViolation("Condition", mapOf("de" to "test")))))
        assertNotEquals(ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", null))), ValidationResult.Invalid(violations = listOf(ConditionViolation("condition", mapOf("de" to "test")), ConditionViolation("condition", null))))

        assertEquals(ValidationResult.Error(failedConditions = listOf()), ValidationResult.Error(failedConditions = listOf()))
        assertEquals(ValidationResult.Error(failedConditions = listOf("test")), ValidationResult.Error(failedConditions = listOf("test")))
        assertEquals(ValidationResult.Error(failedConditions = listOf("test", "Test")), ValidationResult.Error(failedConditions = listOf("test", "Test")))
        assertNotEquals(ValidationResult.Error(failedConditions = listOf("Test", "test")), ValidationResult.Error(failedConditions = listOf("test", "Test")))
        assertNotEquals(ValidationResult.Error(failedConditions = listOf("test", "Test")), ValidationResult.Error(failedConditions = listOf("test", "Test", "X")))

        assertNotEquals(ValidationResult.Invalid(violations = listOf()), ValidationResult.Error(failedConditions = listOf()))
        assertNotEquals(ValidationResult.Valid(result = BusinessRuleValidationResult("", "", listOf(), listOf(), listOf(), listOf())), ValidationResult.Error(failedConditions = listOf()))

        var result1 = BusinessRuleValidationResult("W", "ET", listOf(), listOf(), listOf(), listOf())
        var result2 = BusinessRuleValidationResult("W", "ET", listOf(), listOf(), listOf(), listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))
        ))
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        result1 = BusinessRuleValidationResult("W", "ET", listOf(), listOf(), listOf(), listOf(BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))))
        result2 = BusinessRuleValidationResult("W", "ET", listOf(), listOf(), listOf(), listOf(
            BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2")), BusinessRuleValidationLinkedConditionResult(null, listOf("Test 3"))
        ))
        assertNotEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))

        val time = LocalDateTime.now().toInstant(ZoneOffset.UTC)
        val date = LocalDateTime.ofInstant(time, ZoneOffset.UTC)
        val sameDate = LocalDateTime.ofInstant(time, ZoneOffset.UTC)
        val modifiedInstant = time.minusSeconds(20)
        val anotherDate = LocalDateTime.ofInstant(modifiedInstant, ZoneOffset.UTC)
        val anotherSameDate = LocalDateTime.ofInstant(modifiedInstant, ZoneOffset.UTC)

        result1 = BusinessRuleValidationResult(profile = "W", region = "ET", listOf(ValidityTimeResult(date, ValidityTimeFormat.DATE_TIME, listOf("condition"))), listOf(ValidityTimeResult(anotherDate, ValidityTimeFormat.DATE, listOf())), listOf(BusinessRuleValidationLinkedConditionResult(null, listOf("Condition1"))), listOf(BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))))
        result2 = BusinessRuleValidationResult(profile = "W", region = "ET", listOf(ValidityTimeResult(sameDate, ValidityTimeFormat.DATE_TIME, listOf("condition"))), listOf(ValidityTimeResult(anotherSameDate, ValidityTimeFormat.DATE, listOf())), listOf(BusinessRuleValidationLinkedConditionResult(null, listOf("Condition1"))), listOf(BusinessRuleValidationLinkedConditionResult(null, listOf("Test", "Test 2"))))
        assertEquals(ValidationResult.Valid(result1), ValidationResult.Valid(result2))
    }

    @Test
    fun testValidityTimeResult() {
        val time = LocalDateTime.now().toInstant(ZoneOffset.UTC)
        val date = LocalDateTime.ofInstant(time, ZoneOffset.UTC)
        val sameDate = LocalDateTime.ofInstant(time, ZoneOffset.UTC)
        val anotherDate = LocalDateTime.now().minusSeconds(20)

        assertEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, null), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, null))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, null), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE_TIME, null))
        assertEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, null), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf()))
        assertEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf()), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, null))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("Test")), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, null))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("Test")), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("test")))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("test", "Test")), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("Test", "test")))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("test")), ValidityTimeResult(sameDate, ValidityTimeFormat.DATE, listOf("test", "Test")))

        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, listOf("test")), ValidityTimeResult(anotherDate, ValidityTimeFormat.DATE, listOf("test", "Test")))
        assertNotEquals(ValidityTimeResult(date, ValidityTimeFormat.DATE, null), ValidityTimeResult(anotherDate, ValidityTimeFormat.DATE, null))
    }
}