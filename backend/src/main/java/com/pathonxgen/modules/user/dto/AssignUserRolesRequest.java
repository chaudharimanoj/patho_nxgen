package com.pathonxgen.modules.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssignUserRolesRequest(
        @NotEmpty(message = "At least one role is required")
        List<String> roles
) {}
