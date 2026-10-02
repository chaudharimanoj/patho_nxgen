package com.pathonxgen.modules.auth.dto;

import java.util.List;

public record CurrentUserResponse(String userId, String username, String email, String tenantSlug, List<String> roles) {
}
