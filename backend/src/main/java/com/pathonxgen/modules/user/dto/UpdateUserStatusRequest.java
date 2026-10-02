package com.pathonxgen.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateUserStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {}
