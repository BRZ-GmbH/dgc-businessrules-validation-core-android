package at.gv.brz.brvc

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TestUtil {

    enum class VaccineType(val value: String) {
        Johnson("EU\\/1\\/20\\/1525"),
        Moderna("EU\\/1\\/21\\/1618")
    }

    enum class TestResult(val value: String) {
        Negative("260415000"),
        Positive("123")
    }
    companion object {
        fun generateVaccinationCertificate(vaccinationType: VaccineType, vaccinationAgeInDays: Long, dateOfBirthAgeYears: Long, dateOfBirthAgeDays: Long, doses: Int, sequence: Int): String {
            return generateVaccinationCertificate(vaccinationType.value, vaccinationAgeInDays, dateOfBirthAgeYears, dateOfBirthAgeDays, doses, sequence)
        }

        fun generateVaccinationCertificate(vaccinationType: String, vaccinationAgeInDays: Long, dateOfBirthAgeYears: Long, dateOfBirthAgeDays: Long, doses: Int, sequence: Int): String {
            val vaccinationDate = LocalDateTime.now().minusDays(vaccinationAgeInDays)
            val dateOfBirth = LocalDateTime.now().minusYears(dateOfBirthAgeYears).minusDays(dateOfBirthAgeDays)

            return generateVaccinationCertificate(
                vaccinationType = vaccinationType,
                vaccinationDate = vaccinationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dateOfBirth = dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                doses = doses,
                sequence = sequence
            )
        }

        private fun generateVaccinationCertificate(vaccinationType: String, vaccinationDate: String, dateOfBirth: String, doses: Int, sequence: Int): String {
            return """
        {
            "ver": "1.2.1",
            "nam": {
                "fn": "Doe",
                "gn": "John",
                "fnt": "DOE",
                "gnt": "JOHN"
            },
            "dob": "${dateOfBirth}",
            "v": [
                {
                    "tg": "840539006",
                    "vp": "111\\/9349007",
                    "mp": "${vaccinationType}",
                    "ma": "ORG-100030215",
                    "dn": ${doses},
                    "sd": ${sequence},
                    "dt": "${vaccinationDate}",
                    "co": "AT",
                    "is": "Ministry of Health, Austria",
                    "ci": "URN:UVCI:01:AT:10807843F94AEE0EE5093FBC254BD813#B"
                }
            ]
        }
        """
        }

        fun generateRecoveryCertificate(positiveResultAgeInDays: Long, dateOfBirthAgeYears: Long, dateOfBirthAgeDays: Long): String {
            val positiveResultDate = LocalDateTime.now().minusDays(positiveResultAgeInDays)
            val dateOfBirth = LocalDateTime.now().minusYears(dateOfBirthAgeYears).minusDays(dateOfBirthAgeDays)

            return generateRecoveryCertificate(
                positiveResultDate = positiveResultDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dateOfBirth = dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        }

        private fun generateRecoveryCertificate(positiveResultDate: String, dateOfBirth: String): String {
            return """
        {
            "ver": "1.2.1",
            "nam": {
                "fn": "Doe",
                "gn": "John",
                "fnt": "DOE",
                "gnt": "JOHN"
            },
            "dob": "${dateOfBirth}",
            "r": [
                {
                    "tg": "840539006",
                    "fr": "${positiveResultDate}",
                    "co": "AT",
                    "is": "Ministry of Health, Austria",
                    "df": "2021-04-04",
                    "du": "2021-10-04",
                    "ci": "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
                }
            ]
        }
        """
        }

        fun generateRATTestCertificate(sampleCollectionDateAgeHours: Long, sampleCollectionDateAgeMinutes: Long, dateOfBirthAgeYears: Long, dateOfBirthAgeDays: Long, result: TestResult): String {
            val sampleCollectionDate = LocalDateTime.now().minusHours(sampleCollectionDateAgeHours).minusMinutes(sampleCollectionDateAgeMinutes)
            val dateOfBirth = LocalDateTime.now().minusYears(dateOfBirthAgeYears).minusDays(dateOfBirthAgeDays)

            return generateTestCertificate(
                sampleCollectionDate = sampleCollectionDate.format(DateTimeFormatter.ISO_DATE_TIME),
                dateOfBirth = dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                testType = "LP217198-3",
                result = result.value
            )
        }

        fun generatePCRTestCertificate(sampleCollectionDateAgeHours: Long, sampleCollectionDateAgeMinutes: Long, dateOfBirthAgeYears: Long, dateOfBirthAgeDays: Long, result: TestResult): String {
            val sampleCollectionDate = LocalDateTime.now().minusHours(sampleCollectionDateAgeHours).minusMinutes(sampleCollectionDateAgeMinutes)
            val dateOfBirth = LocalDateTime.now().minusYears(dateOfBirthAgeYears).minusDays(dateOfBirthAgeDays)

            return generateTestCertificate(
                sampleCollectionDate = sampleCollectionDate.format(DateTimeFormatter.ISO_DATE_TIME),
                dateOfBirth = dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                testType = "LP6464-4",
                result = result.value
            )
        }

        private fun generateTestCertificate(sampleCollectionDate: String, dateOfBirth: String, testType: String, result: String): String {
            return """
        {
            "ver": "1.2.1",
            "nam": {
                "fn": "Doe",
                "gn": "John",
                "fnt": "DOE",
                "gnt": "JOHN"
            },
            "dob": "${dateOfBirth}",
            "t": [
                {
                    "tg": "840539006",
                    "tt": "${testType}",
                    "ma": "1232",
                    "sc": "${sampleCollectionDate}",
                    "tr": "${result}",
                    "tc": "Testing center Vienna 1",
                    "co": "AT",
                    "is": "Ministry of Health, Austria",
                    "ci": "URN:UVCI:01:AT:71EE2559DE38C6BF7304FB65A1A451EC#3"
                }
            ]
        }
        """
        }

    }
}