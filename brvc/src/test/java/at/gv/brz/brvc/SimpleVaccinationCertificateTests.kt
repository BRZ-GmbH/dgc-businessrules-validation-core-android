package at.gv.brz.brvc

import at.gv.brz.brvc.model.ValidationResult
import at.gv.brz.brvc.model.ValidityTimeFormat
import at.gv.brz.brvc.model.data.BusinessRuleCertificateType
import at.gv.brz.brvc.model.data.ValidityTime
import at.gv.brz.brvc.model.data.ValidityTimeModifier
import at.gv.brz.brvc.util.dateByModifyingWith
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SimpleVaccinationCertificateTests: BusinessRulesTest() {

    @Test
    fun testSimpleEvaluationForVaccination() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
    }

    @Test
    fun testSimpleEvaluationForVaccinationWithEqualToProfile() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
    }

    @Test
    fun testSimpleEvaluationForExpiredVaccination() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 271, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(1, violations.size)
        assertEquals("isVaccinationDateLessThan270DaysAgo", violations.first().condition)
    }

    @Test
    fun testSimpleEvaluationForPartialVaccination() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 40, 20, 0, 1, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(1, violations.size)
        assertEquals("isFullVaccination", violations.first().condition)
    }

    @Test
    fun testSimpleEvaluationForInvalidVaccine() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate("EU/xxxx", 40, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(1, violations.size)
        assertEquals("isAllowedVaccine", violations.first().condition)
    }

    @Test
    fun testSimpleEvaluationForMultipleGeneralConditionViolations() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate("EU/xxxx", 40, 20, 0, 1, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Invalid)
        val violations = (validationResult as ValidationResult.Invalid).violations
        assertEquals(2, violations.size)
        assertEquals("isAllowedVaccine", violations.first().condition)
        assertEquals("isFullVaccination", violations[1].condition)
    }

    @Test
    fun testSimpleEvaluationForVaccinationWithMaxValidity() {
        val validationCore = getValidationCore("simple_vaccination_max_validity")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.parse("2024-01-01T16:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
    }

    @Test
    fun testSimpleEvaluationForVaccinationWithInvalidMaxValidity() {
        val validationCore = getValidationCore("simple_vaccination_unknown_time_intervals")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 5000)))
    }

    @Test
    fun testSimpleEvaluationForVaccinationWithUnknownTimeIntervals() {
        val validationCore = getValidationCore("simple_vaccination_unknown_time_intervals")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Entry", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
    }

    @Test
    fun testSimpleEvaluationForVaccinationWithoutRuleValidity() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", "Club", certificate)
        Assert.assertNotNull(validationResult)
        assertTrue(validationResult is ValidationResult.Valid)
        val result = (validationResult as ValidationResult.Valid).result
        assertFalse(result.validFrom.isEmpty())
        assertFalse(result.validUntil.isEmpty())
        assertEquals(ValidityTimeFormat.DATE, result.validFrom.first().format)
        assertEquals(ValidityTimeFormat.DATE, result.validUntil.first().format)
        assertTrue(result.validFrom.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30)))
        assertTrue(result.validUntil.first().time.isEqualToTheDayWithDate(LocalDateTime.now().modified(days = -30).modified(days = 270)))
    }

    @Test
    fun testSimpleEvaluationForAllDefinedProfiles() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", null, certificate)
        assertEquals(2, validationResult?.count())
        assertNotNull(validationResult?.get("Entry"))
        assertNotNull(validationResult?.get("Club"))
    }

    @Test
    fun testSimpleEvaluationForMultipleProfiles() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", listOf("Entry", "Club"), certificate)
        assertEquals(2, validationResult?.count())
        assertNotNull(validationResult?.get("Entry"))
        assertNotNull(validationResult?.get("Club"))
    }

    @Test
    fun testSimpleEvaluationForMultipleProfilesWithUnknown() {
        val validationCore = getValidationCore("simple_vaccination")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", listOf("Entry", "Club", "MissingProfile"), certificate)
        assertEquals(2, validationResult?.count())
        assertNotNull(validationResult?.get("Entry"))
        assertNotNull(validationResult?.get("Club"))
    }

    @Test
    fun testSimpleEvaluationOfTimeModifier() {
        val vaccinationDate = LocalDateTime.now().minusDays(30)

        val validationCore = getValidationCore("simple_vaccination_time_modifier")
        val certificate = TestUtil.generateVaccinationCertificate(TestUtil.VaccineType.Moderna, 30, 20, 0, 2, 2)
        val validationResult = validationCore?.evaluateCertificate(certificate, BusinessRuleCertificateType.VACCINATION, ZonedDateTime.now(), ZonedDateTime.now(), "AT", "W", null, certificate)
        assertEquals(2, validationResult?.count())

        val entryValidationResult = validationResult?.get("Entry")
        val clubValidationResult = validationResult?.get("Club")

        assertTrue(entryValidationResult is ValidationResult.Valid)
        val entryResult = (entryValidationResult as ValidationResult.Valid).result
        assertTrue(entryResult.validUntil.first().time.isEqualToTheMinuteWithDate(vaccinationDate.plusDays(180).dateByModifyingWith(ValidityTimeModifier.END_OF_MONTH).dateByModifyingWith(ValidityTimeModifier.END_OF_DAY)))

        assertTrue(clubValidationResult is ValidationResult.Valid)
        val clubResult = (clubValidationResult as ValidationResult.Valid).result
        assertTrue(clubResult.validUntil.first().time.isEqualToTheMinuteWithDate(vaccinationDate.plusDays(180).dateByModifyingWith(ValidityTimeModifier.END_OF_DAY)))
    }
}