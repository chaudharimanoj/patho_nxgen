package com.pathonxgen.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTenantUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100)
        String password,

        @NotBlank(message = "Display name is required")
        String displayName,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        String employeeCode,
        String department,
        String designation,

        @NotEmpty(message = "At least one role must be assigned")
        List<String> roles
) {}
