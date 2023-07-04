package at.gv.brz.brvc

import at.gv.brz.brvc.util.personGroupingIdentifierForDGCCertificate
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupingIdentifierTests {

    @Test
    fun testGroupingIdentifierNormalization() {
        assertEquals("mustermann_max_1980-01-01", String.personGroupingIdentifierForDGCCertificate("Mustermann", "Max Peter", "1980-01-01"))
        assertEquals("mustermann_max_1980-01-01", String.personGroupingIdentifierForDGCCertificate("Mustermann", "Max-Peter", "1980-01-01"))
        assertEquals("mustermann_max_1980-01-01", String.personGroupingIdentifierForDGCCertificate("Mustermann-Mueller", "Max-Peter", "1980-01-01"))
    }

    @Test
    fun testEmptyValues() {
        assertEquals("_max_1980-01-01", String.personGroupingIdentifierForDGCCertificate("", "Max Peter", "1980-01-01"))
        assertEquals("_max_1980-01-01", String.personGroupingIdentifierForDGCCertificate(null, "Max Peter", "1980-01-01"))
        assertEquals("mustermann__1980-01-01", String.personGroupingIdentifierForDGCCertificate("Mustermann", "", "1980-01-01"))
        assertEquals("mustermann__1980-01-01", String.personGroupingIdentifierForDGCCertificate("Mustermann", null, "1980-01-01"))
        assertEquals("mustermann_max_", String.personGroupingIdentifierForDGCCertificate("Mustermann", "Max Peter", ""))
        assertEquals("mustermann_max_", String.personGroupingIdentifierForDGCCertificate("Mustermann", "Max-Peter", null))
    }
}