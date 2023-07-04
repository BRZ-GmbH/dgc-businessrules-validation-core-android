package at.gv.brz.brvc

import at.gv.brz.brvc.model.ValidationResult
import at.gv.brz.brvc.model.ValidityTimeFormat
import at.gv.brz.brvc.model.data.BusinessRuleCertificateType
import junit.framework.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

class ExternalConditionTests: BusinessRulesTest() {

    @Test
    fun testVaccinationWithAlwaysTrueEvaluation() {
        val validationCore = getValidationCore("simple_test_with_external_condition", ExternalConditionEvaluationStrategy.DEFAULT_TO_TRUE)
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
        assertEquals(1, result.matchingLinkedConditions.size)
        assertNotNull(result.matchingLinkedConditions.first().conditions)
    }

    @Test
    fun testVaccinationWithAlwaysFalseEvaluation() {
        val validationCore = getValidationCore("simple_test_with_external_condition", ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE)
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
        assertEquals(1, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().conditions)
    }

    @Test
    fun testVaccinationWithAlwaysFalseAndTrueEvaluationEvaluation() {
        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            assertEquals("W", region)
            assertEquals("2G+", profile)
            assertEquals("Vaccination", ruleId)
            assertEquals("vaccination", ruleCertificateType)
            true
        }
        val validationCore = getValidationCore("simple_test_with_external_condition", ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE)
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
        assertEquals(1, result.matchingLinkedConditions.size)
        assertNotNull(result.matchingLinkedConditions.first().conditions)
    }

    @Test
    fun testPCRTestWithAlwaysFalseAndTrueEvaluationEvaluation() {
        val validationCore = getValidationCore("simple_test_with_external_condition", ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE)
        var hasCheckedVaccinationCondition = false
        var hasCheckedRecoveryCondition = false
        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            assertEquals("NOE", region)
            if (condition == "hasValidVaccinationCertificateForPerson") {
                hasCheckedVaccinationCondition = true
                assertTrue(parameters.isEmpty())
                true
            } else if (condition == "hasValidRecoveryCertificateForPerson") {
                hasCheckedRecoveryCondition = true
                assertEquals("valueX", parameters["parameterX"])
                false
            } else {
                true
            }
        }
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 14, 0, TestUtil.TestResult.Negative)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "NOE", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 72)))
        assertEquals(1, result.matchingLinkedConditions.size)
        assertNotNull(result.matchingLinkedConditions.first().violationMessage)
        assertFalse(result.matchingLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidVaccinationCertificateForPerson", result.matchingLinkedConditions.first().conditions.first())
        assertEquals(1, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().violationMessage)
        assertFalse(result.violatedOrFailedLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidRecoveryCertificateForPerson_parameterX:valueX", result.violatedOrFailedLinkedConditions.first().conditions.first())
        assertTrue(hasCheckedRecoveryCondition)
        assertTrue(hasCheckedVaccinationCondition)
    }

    @Test
    fun testPCRTestWithDifferentValidityBasedOnExternalCondition() {
        val validationCore = getValidationCore("simple_test_with_external_condition_and_validity", ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE)
        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            condition == "hasValidVaccinationCertificateForPerson"
        }
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 14, 0, TestUtil.TestResult.Negative)
        var validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "NOE", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        var result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 144)))
        assertEquals(1, result.matchingLinkedConditions.size)
        assertNotNull(result.matchingLinkedConditions.first().violationMessage)
        assertFalse(result.matchingLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidVaccinationCertificateForPerson", result.matchingLinkedConditions.first().conditions.first())
        assertEquals(1, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().violationMessage)
        assertFalse(result.violatedOrFailedLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidRecoveryCertificateForPerson_parameterX:valueX", result.violatedOrFailedLinkedConditions.first().conditions.first())

        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            false
        }

        validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "NOE", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 100)))
        assertEquals(0, result.matchingLinkedConditions.size)
        assertEquals(2, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().violationMessage)
        assertFalse(result.violatedOrFailedLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidVaccinationCertificateForPerson", result.violatedOrFailedLinkedConditions.first().conditions.first())
        assertEquals("ext.hasValidRecoveryCertificateForPerson_parameterX:valueX", result.violatedOrFailedLinkedConditions.last().conditions.first())
    }

    @Test
    fun testPCRTestWithDifferentValidityBasedOnExternalConditionWithFallback() {
        val validationCore = getValidationCore("simple_test_with_external_condition_and_validity_and_fallback", ExternalConditionEvaluationStrategy.DEFAULT_TO_FALSE)
        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            condition == "hasValidVaccinationCertificateForPerson"
        }
        val certificate = TestUtil.generatePCRTestCertificate(47, 59, 14, 0, TestUtil.TestResult.Negative)
        var validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "NOE", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        var result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 144)))
        assertEquals(1, result.matchingLinkedConditions.size)
        assertNotNull(result.matchingLinkedConditions.first().violationMessage)
        assertFalse(result.matchingLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidVaccinationCertificateForPerson", result.matchingLinkedConditions.first().conditions.first())
        assertEquals(1, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().violationMessage)
        assertFalse(result.violatedOrFailedLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidRecoveryCertificateForPerson_parameterX:valueX", result.violatedOrFailedLinkedConditions.first().conditions.first())

        externalConditionEvaluator.evaluationBlock = { condition, parameters, ruleId, ruleCertificateType, region, profile, originalCertificateObject ->
            false
        }

        validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.TEST, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "NOE", "2G+", certificate)
        assertTrue(validationResult is ValidationResult.Valid)
        result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE_TIME, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(hours = -47, minutes = -59).modified(hours = 72)))
        assertEquals(0, result.matchingLinkedConditions.size)
        assertEquals(2, result.violatedOrFailedLinkedConditions.size)
        assertNotNull(result.violatedOrFailedLinkedConditions.first().violationMessage)
        assertFalse(result.violatedOrFailedLinkedConditions.first().conditions.isEmpty())
        assertEquals("ext.hasValidVaccinationCertificateForPerson", result.violatedOrFailedLinkedConditions.first().conditions.first())
        assertEquals("ext.hasValidRecoveryCertificateForPerson_parameterX:valueX", result.violatedOrFailedLinkedConditions.last().conditions.first())
    }
}