package at.gv.brz.brvc

import at.gv.brz.brvc.model.BusinessRulesSyntaxError
import at.gv.brz.brvc.model.data.BusinessRuleCertificateType
import org.junit.Assert.*
import org.junit.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class RuleValidityTests: BusinessRulesTest() {

    @Test
    fun testValidationBeforeValidFrom() {
        val validationCore = getValidationCore("simple_vaccination", validationClock = ZonedDateTime.parse("2021-01-01T21:59:59Z", DateTimeFormatter.ISO_DATE_TIME))
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNull(validationResult)
    }

    @Test
    fun testValidationAfterValidUntil() {
        val validationCore = getValidationCore("simple_vaccination", validationClock = ZonedDateTime.parse("2030-06-01T00:00:01Z", DateTimeFormatter.ISO_DATE_TIME))
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        assertNull(validationResult)
    }

    @Test
    fun testInvalidTargetGroup() {
        val parsedPayload = getBusinessRules("validation_unknown_group")
        val validation = parsedPayload!!.validate()
        assertEquals(1, validation.size)
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnavailableTargetGroup("childrenUnknown")))
    }

    @Test
    fun testInvalidMultichain() {
        val parsedPayload = getBusinessRules("validation_unallowed_multichain")
        val validation = parsedPayload!!.validate()
        assertEquals(1, validation.size)
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnallowedMultistepProfileChain("Club", "all")))
    }

    @Test
    fun testUnknownCondition() {
        val parsedPayload = getBusinessRules("validation_unknown_condition")
        val validation = parsedPayload!!.validate()
        assertEquals(4, validation.size)
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnavailableCondition("isSomeUnknownCertificateTypeCondition")))
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnavailableCondition("isSomeUnknownGeneralCondition")))
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnavailableCondition("isSomeUnknownCondition")))
        assertTrue(validation.contains(BusinessRulesSyntaxError.UnavailableCondition("isAnotherUnknownCondition")))
    }
}