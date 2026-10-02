package com.pathonxgen.core.exception;

/**
 * Thrown when a request resolves to a tenant schema that does not match
 * the record being accessed. Surfaced as 403, never leaks which tenant
 * the record actually belongs to.
 */
public class TenantAccessDeniedException extends RuntimeException {
    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
