package com.ispf.driver.snmp;

import com.ispf.core.model.DataRecord;
import org.junit.jupiter.api.Test;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnmpValueMapperTest {

    @Test
    void mapsNumericSnmpVariable() throws Exception {
        DataRecord record = SnmpValueMapper.toRecord(new Gauge32(42), SnmpPoint.ValueKind.AUTO);
        assertEquals(42.0, record.firstRow().get("value"));
    }

    @Test
    void mapsStringSnmpVariable() throws Exception {
        DataRecord record = SnmpValueMapper.toRecord(new OctetString("router-01"), SnmpPoint.ValueKind.AUTO);
        assertEquals("router-01", record.firstRow().get("value"));
    }

    @Test
    void buildsIntegerVariableForSet() throws Exception {
        DataRecord record = DataRecord.single(
                com.ispf.core.model.DataSchema.builder("x").field("value", com.ispf.core.model.FieldType.INTEGER).build(),
                java.util.Map.of("value", 7)
        );
        assertEquals(7, ((Integer32) SnmpValueMapper.fromRecord(record, SnmpPoint.ValueKind.INTEGER)).getValue());
    }
}
