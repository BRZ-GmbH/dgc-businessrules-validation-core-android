package at.gv.brz.brvc

import at.gv.brz.brvc.model.ValidationResult
import at.gv.brz.brvc.model.ValidityTimeFormat
import at.gv.brz.brvc.model.data.BusinessRuleCertificateType
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

class SimpleTestCertificateTests: BusinessRulesTest() {

    @Test
    fun testSimpleEvaluationForPCRTestOf8YearOld() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(52, 0, 8, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheMinuteWithDate(LocalDateTime.now().modified(hours = -52)))
        assertTrue(result.validUntil.first().time.isEqualToTheMinuteWithDate(LocalDateTime.now().modified(hours = -52).modified(hours = 72)))
    }

    @Test
    fun testSimpleEvaluationForPCRTestOfAdult() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(52, 0, 24, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(0, violations.size)

    }

    @Test
    fun testSimpleEvaluationForPCRTestOf13YearOld() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 14, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheMinuteWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheMinuteWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 48)))
    }

    @Test
    fun testSimpleEvaluationForPCRTestOf13YearOldInNightClub() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 14, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(0, violations.size)
    }

    @Test
    fun testSimpleEvaluationForPCRTestOfAdultInNightClub() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(52, 0, 24, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(0, violations.size)

    }

    @Test
    fun testSimpleEvaluationForPCRTestOf4YearOld() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 4, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNull(validationResult)
    }

    @Test
    fun testSimpleEvaluationForPCRTestOf4YearOldInNightClub() {
        val validationCore = getValidationCore("simple_tests")
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 4, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(0, violations.size)
    }

    @Test
    fun testNonMatchingCertificateType() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 4, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNull(validationResult)
    }
}