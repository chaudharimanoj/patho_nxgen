package com.pathonxgen.core.storage;

import com.pathonxgen.core.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Storage Path Manager enforcing Section 39 & Rule 6:
 * "Tenant files should use tenant-specific storage paths: /tenants/{tenant_id}/{module}/{filename}".
 */
@Service
public class TenantStorageService {

    public String resolveObjectKey(String module, String filename) {
        UUID tenantId = TenantContext.getTenantId();
        String prefix = tenantId != null ? tenantId.toString() : "shared";
        String cleanModule = module != null ? module.toLowerCase().replaceAll("[^a-z0-9_-]", "") : "documents";
        String cleanFilename = filename != null ? filename.replaceAll("[^a-zA-Z0-9_.-]", "_") : "file.bin";
        return "tenants/" + prefix + "/" + cleanModule + "/" + cleanFilename;
    }
}
