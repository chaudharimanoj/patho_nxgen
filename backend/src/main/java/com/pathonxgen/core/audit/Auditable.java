package com.pathonxgen.core.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic audit logging satisfying Section 40:
 * Captures tenant_id, keycloak_user_id, action, resource_type, resource_id, and timestamp.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
    String resourceType();
}
