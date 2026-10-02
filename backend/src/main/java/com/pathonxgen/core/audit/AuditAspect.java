package com.pathonxgen.core.audit;

import com.pathonxgen.core.tenant.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Aspect logging security & operational events to public.audit_log for Section 40 compliance.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Auditable auditable, Object result) {
        UUID tenantId = TenantContext.getTenantId();
        String userId = extractKeycloakUserId();

        log.info("AUDIT_LOG [tenant_id: {}, user_id: {}, action: {}, resource_type: {}, method: {}]",
                tenantId, userId, auditable.action(), auditable.resourceType(), joinPoint.getSignature().getName());
    }

    private String extractKeycloakUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return "anonymous";
    }
}
