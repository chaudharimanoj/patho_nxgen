package com.pathonxgen.modules.integration.service;

/**
 * Placeholder adapter boundary for laboratory analyzer integrations (HL7 /
 * ASTM protocols over serial or TCP). Each analyzer vendor gets its own
 * adapter implementing a shared AnalyzerResultListener interface, so the
 * laboratory module only depends on the interface, never a vendor SDK.
 */
public class LabAnalyzerIntegrationService {
}
