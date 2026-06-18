package com.ispf.driver.snmp;

import com.ispf.core.model.DataRecord;
import org.junit.jupiter.api.Test;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnmpPointTest {

    @Test
    void parsesPlainOid() throws Exception {
        SnmpPoint point = SnmpPoint.parse("1.3.6.1.2.1.1.5.0");
        assertEquals("1.3.6.1.2.1.1.5.0", point.oid());
        assertEquals(SnmpPoint.ValueKind.AUTO, point.valueKind());
    }

    @Test
    void parsesOidWithValueKind() throws Exception {
        SnmpPoint point = SnmpPoint.parse("1.3.6.1.2.1.1.5.0:STRING");
        assertEquals(SnmpPoint.ValueKind.STRING, point.valueKind());
    }

    @Test
    void rejectsInvalidMapping() {
        assertThrows(Exception.class, () -> SnmpPoint.parse(" "));
    }
}
