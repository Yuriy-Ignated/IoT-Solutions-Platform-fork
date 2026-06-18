package com.ispf.driver.snmp;

import com.ispf.driver.DriverException;

/**
 * Parsed SNMP OID mapping. Format: {@code oid} or {@code oid:VALUE_KIND}.
 * VALUE_KIND: AUTO (default), INTEGER, STRING, BOOLEAN.
 */
record SnmpPoint(String oid, ValueKind valueKind) {

    enum ValueKind {
        AUTO, INTEGER, STRING, BOOLEAN
    }

    static SnmpPoint parse(String mapping) throws DriverException {
        if (mapping == null || mapping.isBlank()) {
            throw new DriverException("SNMP mapping is empty");
        }
        String trimmed = mapping.trim();
        int colon = trimmed.indexOf(':');
        if (colon < 0) {
            return new SnmpPoint(trimmed, ValueKind.AUTO);
        }
        String oid = trimmed.substring(0, colon).trim();
        String kindRaw = trimmed.substring(colon + 1).trim();
        if (oid.isBlank()) {
            throw new DriverException("SNMP OID is required in mapping: " + mapping);
        }
        try {
            return new SnmpPoint(oid, ValueKind.valueOf(kindRaw.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new DriverException("Unknown SNMP value kind in mapping: " + mapping, e);
        }
    }
}
