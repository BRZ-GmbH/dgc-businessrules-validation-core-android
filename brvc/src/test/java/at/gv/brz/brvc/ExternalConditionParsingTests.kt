package at.gv.brz.brvc

import at.gv.brz.brvc.util.externalConditionNameAndArguments
import at.gv.brz.brvc.util.isExternalCondition
import org.junit.Assert.*
import org.junit.Test

class ExternalConditionParsingTests {
    @Test
    fun testExternalConditionCheck() {
        assertTrue("ext.".isExternalCondition())
        assertTrue("ext.Test".isExternalCondition())
        assertFalse(" ext.".isExternalCondition())
        assertTrue("ext.ext.".isExternalCondition())
        assertFalse("Ext.".isExternalCondition())
        assertFalse("ext:".isExternalCondition())
    }

    @Test
    fun testExternalConditionNameWithArguments() {
        var parsedConditions = "ext.hasSomeCertificate".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertEquals("hasSomeCertificate", parsedConditions!!.condition)
        assertTrue(parsedConditions.parameters.isEmpty())

        parsedConditions = "ext.hasSomeCertificate(withBracket)".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertEquals("hasSomeCertificate(withBracket)", parsedConditions!!.condition)
        assertTrue(parsedConditions.parameters.isEmpty())
    }

    @Test
    fun testInvalidExternalConditionName() {
        assertNull("hasSomeCertificate".externalConditionNameAndArguments())
        assertNull(" ext.hasSomeCertificate".externalConditionNameAndArguments())
        assertNull("test.ext.hasSomeCertificate".externalConditionNameAndArguments())
    }

    @Test
    fun testExternalConditionWithMultipleParameters() {
        var parsedConditions = "ext.hasSomeCertificate_type:test_24hours".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertEquals("hasSomeCertificate", parsedConditions!!.condition)
        assertEquals(1, parsedConditions.parameters.size)
        assertEquals("test", parsedConditions.parameters["type"])

        parsedConditions = "ext.hasSomeCertificate_type:Recovery_duration:180_unit:Days".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertEquals("hasSomeCertificate", parsedConditions!!.condition)
        assertEquals(3, parsedConditions.parameters.size)
        assertEquals("Recovery", parsedConditions.parameters["type"])
        assertEquals("180", parsedConditions.parameters["duration"])
        assertEquals("Days", parsedConditions.parameters["unit"])
    }

    @Test
    fun testEmptyValues() {
        var parsedConditions = "ext.".externalConditionNameAndArguments()
        assertNull(parsedConditions)

        parsedConditions = "ext.hasSomeCertificate_".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertTrue(parsedConditions!!.parameters.isEmpty())

        parsedConditions = "ext.hasSomeCertificate___".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertTrue(parsedConditions!!.parameters.isEmpty())

        parsedConditions = "ext.hasSomeCertificate_test___duration:24_unit:hours__".externalConditionNameAndArguments()
        assertNotNull(parsedConditions)
        assertEquals("24", parsedConditions!!.parameters["duration"])
        assertEquals("hours", parsedConditions.parameters["unit"])
    }
}